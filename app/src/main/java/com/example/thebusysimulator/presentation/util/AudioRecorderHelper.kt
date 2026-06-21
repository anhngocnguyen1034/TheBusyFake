package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: String? = null

    fun startRecording(): String {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file.absolutePath

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(outputFile)
            prepare()
            start()
        }
        return outputFile!!
    }

    // Returns the file path on success, null if recording was too short or failed
    fun stopRecording(): String? {
        return try {
            recorder?.apply { stop(); release() }
            recorder = null
            val path = outputFile
            outputFile = null
            path
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            outputFile?.let { File(it).delete() }
            outputFile = null
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply { stop(); release() }
        } catch (_: Exception) {}
        recorder = null
        outputFile?.let { File(it).delete() }
        outputFile = null
    }

    val isRecording: Boolean get() = recorder != null
}
