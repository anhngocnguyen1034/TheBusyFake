#!/bin/bash

# ─────────────────────────────────────────────
# Cấu hình
# ─────────────────────────────────────────────
GITHUB_OWNER="anhngocnguyen1034"
GITHUB_REPO="TheBusyFake"
# GITHUB_TOKEN: truyền từ Jenkins qua biến môi trường (Jenkins Credential)
GITHUB_TOKEN="${GITHUB_TOKEN:-}"

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
# Upload APK lên GitHub Releases
# ─────────────────────────────────────────────
upload_to_github_release() {
    local file="$1"
    local tag="$2"
    local file_name
    file_name=$(basename "$file")

    if [ -z "$GITHUB_TOKEN" ]; then
        echo "WARN: GITHUB_TOKEN không được set, bỏ qua upload GitHub Release" >&2
        return
    fi

    echo "Tạo GitHub Release: $tag" >&2

    # Tạo release (bỏ qua lỗi nếu đã tồn tại)
    release_response=$(curl -sS -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Content-Type: application/json" \
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases" \
        -d "{
            \"tag_name\": \"$tag\",
            \"name\": \"$tag [$current_branch]\",
            \"body\": \"Auto build từ Jenkins\\nBranch: $current_branch\\nBuild: #${BUILD_NUMBER:-?}\",
            \"draft\": false,
            \"prerelease\": $([ "$current_branch" == "main" ] && echo 'false' || echo 'true')
        }")

    # Lấy upload_url (xử lý cả trường hợp release đã tồn tại)
    upload_url=$(echo "$release_response" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('upload_url',''))" 2>/dev/null | sed 's/{?name,label}//')

    if [ -z "$upload_url" ]; then
        # Release đã tồn tại, lấy lại upload_url
        echo "Release đã tồn tại, lấy lại upload_url..." >&2
        upload_url=$(curl -sS \
            -H "Authorization: token $GITHUB_TOKEN" \
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/tags/$tag" \
            | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('upload_url',''))" 2>/dev/null | sed 's/{?name,label}//')
    fi

    if [ -z "$upload_url" ]; then
        echo "WARN: Không lấy được upload_url, bỏ qua upload" >&2
        return
    fi

    echo "Upload: $file_name → GitHub Release $tag" >&2
    asset_response=$(curl -sS -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Content-Type: application/octet-stream" \
        "$upload_url?name=$file_name" \
        --data-binary @"$file")

    # Lấy download URL — chỉ echo ra stdout để caller nhận được
    local result
    result=$(echo "$asset_response" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('browser_download_url',''))" 2>/dev/null)
    echo "Download URL: $result" >&2
    echo "$result"
}

# ─────────────────────────────────────────────
# Gửi thông báo Discord khi build thành công + QR
# ─────────────────────────────────────────────
notify_discord_success() {
    local file="$1"
    local elapsed_seconds="$2"
    local download_url="$3"
    local file_name
    file_name=$(basename "$file")
    local size
    size=$(ls -lh "$file" | awk '{print $5}')

    local qr=""
    local description

    if [ -n "$download_url" ]; then
        # Tạo QR từ download URL (dùng api.qrserver.com public, không cần server)
        local encoded_url
        encoded_url=$(python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1], safe=''))" "$download_url")
        qr="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$encoded_url"

        description=$(printf "File: [%s](%s)\nTag: *%s*\nBranch: *%s*\nSize: *%s*\nBuild time: *%ss*" \
            "$file_name" "$download_url" \
            "$newTag" "$current_branch" \
            "$size" "$elapsed_seconds")
    else
        description=$(printf "File: \`%s\`\nTag: *%s*\nBranch: *%s*\nSize: *%s*\nBuild time: *%ss*\n⚠️ Không có GitHub Token — APK không được upload" \
            "$file_name" \
            "$newTag" "$current_branch" \
            "$size" "$elapsed_seconds")
    fi

    local JSON_PAYLOAD
    if [ -n "$qr" ]; then
        JSON_PAYLOAD=$(jq -n \
            --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
            --arg avatar_url "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
            --arg title "✅ Build Success - $current_branch" \
            --arg url "$download_url" \
            --arg description "$description" \
            --arg image_url "$qr" \
            '{username: $username, avatar_url: $avatar_url, embeds: [{title: $title, url: $url, description: $description, color: 3066993, image: {url: $image_url}}]}')
    else
        JSON_PAYLOAD=$(jq -n \
            --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
            --arg avatar_url "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
            --arg title "✅ Build Success - $current_branch" \
            --arg description "$description" \
            '{username: $username, avatar_url: $avatar_url, embeds: [{title: $title, description: $description, color: 3066993}]}')
    fi

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
        --arg avatar_url "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
        --arg title "❌ Build THẤT BẠI - $current_branch" \
        --arg description "Branch: \`$current_branch\`\nCommit: \`$commit\`\nBuild: [#${BUILD_NUMBER:-?}]($build_url)" \
        '{username: $username, avatar_url: $avatar_url, embeds: [{title: $title, description: $description, color: 15158332}]}')

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

# Upload và notify từng file
for file in $(find app/build -type f \( -name "*.apk" -o -name "*.aab" \)); do
    echo "Processing: $file"
    download_url=$(upload_to_github_release "$file" "$newTag")
    notify_discord_success "$file" "$elapsed_seconds" "$download_url"
done

echo "================================================"
echo " Build hoàn tất: v$newTag ($elapsed_seconds giây)"
echo "================================================"

trap - ERR
