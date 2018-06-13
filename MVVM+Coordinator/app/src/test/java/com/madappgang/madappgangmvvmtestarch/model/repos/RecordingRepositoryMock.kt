package com.madappgang.madappgangmvvmtestarch.model.repos

import com.madappgang.madappgangmvvmtestarch.model.models.SourceFile

/**
 * Created by Serhii Chaban sc@madappgang.com on 13.06.18.
 */
class RecordingRepositoryMock(val filesCount:Int) : RecordingRepository {
    override fun get(folder: String, id: String): SourceFile {
        return generateMockFiles(folder).first { it.id == id }
    }

    override fun get(portion: RecordingRepository.DataPortion): List<SourceFile> {
        return generateMockFiles(folder = portion.folder)
    }

    private fun generateMockFiles(folder: String) =
            (1..filesCount).map { SourceFile("file$it.mp4", "file$it.mp4", "/$folder", false) }
}