package com.example.heygongcsounddetect.pcm

class PCMDataProcessor(
    private val pcmData: List<Short> = listOf(),
    //private val sampleRate: Int = 48000

) {

    fun calculateAverageOfTopTenPercent(): Double {
        if (pcmData.isEmpty()) return 0.0

        val sortedData = pcmData.sorted() // Step 1: Sort the data
        val topTenPercentIndex =
            (sortedData.size * 0.9).toInt() // Calculate the start index for the top 10%
        val topTenPercentValues =
            sortedData.subList(topTenPercentIndex, sortedData.size) // Step 2: Select the top 10%

        // Step 3: Calculate the average of these values

        return topTenPercentValues.average()
    }



}
