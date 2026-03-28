#!/bin/bash

# ─────────────────────────────────────────────
# Cấu hình
# ─────────────────────────────────────────────
# WEBHOOKS nhận từ Jenkins qua biến môi trường
DISCORD_WEBHOOK_SUCCESS="${WEBHOOK_SUCCESS:-}"
DISCORD_WEBHOOK_JENKINS="${WEBHOOK_JENKINS:-}"

BUILD_FILE="app/build.gradle.kts"
current_branch="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
start_time=$(date +%s)

echo "================================================"
echo " Branch: $current_branch"
echo " Build file: $BUILD_FILE"
echo "================================================"

# ─────────────────────────────────────────────
# Bỏ qua nếu không phải nhánh chính
# ─────────────────────────────────────────────
if [ "$current_branch" != "develop" ] && \
   [ "$current_branch" != "testing" ] && \
   [ "$current_branch" != "release" ] && \
   [ "$current_branch" != "main" ]; then
    echo "Nhánh '$current_branch' không cần build tự động. Bỏ qua."
    exit 0
fi

# ─────────────────────────────────────────────
# Lấy tag hiện tại từ remote
# ─────────────────────────────────────────────
get_current_tag() {
    git fetch origin --tags 2>/dev/null || true
    latest_tag=$(git ls-remote --tags origin | awk -F'/' '{print $NF}' | grep -v '\^{}' | sed 's/^v//' | sort -V | tail -1)
    echo "DEBUG: latest_tag: $latest_tag" >&2
    if [ -z "$latest_tag" ]; then
        latest_tag="1.0.0.0"
    fi
    echo "$latest_tag"
}

# ─────────────────────────────────────────────
# Tăng version theo nhánh
# ─────────────────────────────────────────────
increase_tag() {
    local branch_name="$1"
    local current_tag
    current_tag=$(get_current_tag)
    local version
    version=$(echo "$current_tag" | sed 's/^v//' | tr -d '\r')

    if [ "$branch_name" == "develop" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$4++; print}')
    elif [ "$branch_name" == "testing" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$3++; $4=0; print}')
    elif [ "$branch_name" == "release" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$2++; $3=0; $4=0; print}')
    elif [ "$branch_name" == "main" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$1++; $2=0; $3=0; $4=0; print}')
    fi

    version=$(echo "$version" | tr -d '\r\n')
    echo "$version"
}

# ─────────────────────────────────────────────
# Cập nhật versionName và versionCode trong build.gradle.kts
# ─────────────────────────────────────────────
update_version_in_gradle() {
    local new_tag="$1"
    local new_code="$2"

    echo "Cập nhật $BUILD_FILE → versionName=$new_tag, versionCode=$new_code"
    sed -i.bak "s/versionName = \"[^\"]*\"/versionName = \"$new_tag\"/" "$BUILD_FILE"
    sed -i.bak "s/versionCode = [0-9]*/versionCode = $new_code/" "$BUILD_FILE"
    rm -f "$BUILD_FILE.bak"

    echo "Sau khi cập nhật:"
    grep -E 'versionName|versionCode' "$BUILD_FILE"
}

# ─────────────────────────────────────────────
# Build theo nhánh
# ─────────────────────────────────────────────
run_builder() {
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    echo "local.properties:"; cat local.properties

    rm -rf app/build/
    chmod +x ./gradlew
    export GRADLE_OPTS="-Dorg.gradle.daemon=false"

    if [ "$current_branch" == "develop" ] || [ "$current_branch" == "testing" ]; then
        echo "--- assembleDebug ---"
        ./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tee /tmp/gradle_build.log
        GRADLE_EXIT=${PIPESTATUS[0]}
        tail -50 /tmp/gradle_build.log
        if [ $GRADLE_EXIT -ne 0 ]; then
            echo "Gradle assembleDebug thất bại!"
            return 1
        fi

    elif [ "$current_branch" == "release" ] || [ "$current_branch" == "main" ]; then
        echo "--- assembleRelease ---"
        ./gradlew assembleRelease --no-daemon --stacktrace 2>&1
        GRADLE_EXIT=$?
        if [ $GRADLE_EXIT -ne 0 ]; then
            echo "Gradle assembleRelease thất bại!"
            return 1
        fi
        echo "--- bundleRelease ---"
        ./gradlew :app:bundleRelease --no-daemon --stacktrace 2>&1
    fi

    echo "--- APK/AAB tìm thấy ---"
    find . -name "*.apk" -o -name "*.aab" 2>/dev/null | grep -v ".gradle" || true
}

# ─────────────────────────────────────────────
# Tạo git tag và push
# ─────────────────────────────────────────────
auto_create_tag() {
    echo "DEBUG: auto_create_tag newTag=$newTag"
    git config user.email "jenkins@ci.local"
    git config user.name "Jenkins CI"

    git tag -d "$newTag" 2>/dev/null || true
    git fetch origin --tags --force

    if git ls-remote --tags origin | grep -q "refs/tags/$newTag$"; then
        echo "Tag $newTag đã tồn tại trên remote, bỏ qua."
        return 0
    fi

    git tag -a "$newTag" -m "[$current_branch] Auto create tag $newTag"
    git push origin "$newTag"
    echo "Tag đã push: $newTag"
}

# ─────────────────────────────────────────────
# Gửi thông báo Discord khi build thành công
# ─────────────────────────────────────────────
notify_discord_success() {
    local file="$1"
    local elapsed_seconds="$2"
    local file_name
    file_name=$(basename "$file")
    local size
    size=$(ls -lh "$file" | awk '{print $5}')

    local JSON_PAYLOAD
    JSON_PAYLOAD=$(jq -n \
        --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
        --arg title "✅ Build Success - $current_branch" \
        --arg description "File: \`$file_name\`\nTag: *$newTag*\nBranch: *$current_branch*\nSize: *$size*\nBuild time: *${elapsed_seconds}s*" \
        '{username: $username, embeds: [{title: $title, description: $description, color: 3066993}]}')

    curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_SUCCESS"
}

# ─────────────────────────────────────────────
# Gửi thông báo Discord khi build thất bại
# ─────────────────────────────────────────────
notify_discord_failure() {
    local commit="${GIT_COMMIT:-$(git log -1 --pretty=format:'%h - %s')}"
    local build_url="${BUILD_URL:-}"

    local JSON_PAYLOAD
    JSON_PAYLOAD=$(jq -n \
        --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
        --arg title "❌ Build THẤT BẠI - $current_branch" \
        --arg description "Branch: \`$current_branch\`\nCommit: \`$commit\`\nBuild: [#${BUILD_NUMBER:-?}]($build_url)" \
        '{username: $username, embeds: [{title: $title, description: $description, color: 15158332}]}')

    curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_JENKINS"
}

# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────

trap 'notify_discord_failure; exit 1' ERR

# Tính version mới
newTag=$(increase_tag "$current_branch")
IFS='.' read -r vmain vrelease vtesting vdevelop <<< "$newTag"

vmain=$((10#${vmain:-0}))
vrelease=$((10#${vrelease:-0}))
vtesting=$((10#${vtesting:-0}))
vdevelop=$((10#${vdevelop:-0}))

calculated_version_code=$(( vmain * 1000000 + vrelease * 10000 + vtesting * 100 + vdevelop ))
if [ "$calculated_version_code" -le 0 ]; then
    calculated_version_code=1
fi

echo "Version mới: $newTag → versionCode=$calculated_version_code"

update_version_in_gradle "$newTag" "$calculated_version_code"
run_builder

echo "--- Cây output ---"
find app/build -type f | sort

apk_count=$(find app/build -type f \( -name "*.apk" -o -name "*.aab" \) | wc -l | tr -d ' ')
if [ "$apk_count" -eq 0 ]; then
    echo "Không tìm thấy APK/AAB sau khi build!"
    notify_discord_failure
    exit 1
fi

auto_create_tag

end_time=$(date +%s)
elapsed_seconds=$((end_time - start_time))

for file in $(find app/build -type f \( -name "*.apk" -o -name "*.aab" \)); do
    echo "Notify Discord: $file"
    notify_discord_success "$file" "$elapsed_seconds"
done

echo "================================================"
echo " Build hoàn tất: v$newTag ($elapsed_seconds giây)"
echo "================================================"

trap - ERR
