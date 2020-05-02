package ltd.abtech.exophyta.tracks.internal

import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.hls.HlsTrackMetadataEntry
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import ltd.abtech.exophyta.tracks.TrackMimeType

//TODO Maybe wrong
internal fun ExoPlayer.getHlsSelectedAudioGroupId(): String? {

    val audioGroupsId = mutableListOf<String>()

    //assume that the first adaptiveTrackSelection is our video playlist
    val selections = currentTrackSelections.all.filterNotNull()
        .filterIsInstance(AdaptiveTrackSelection::class.java)

    Log.d("exophyta", "Count of adaptiveTrackSelection: ${selections.size}")

    val adaptiveTrackSelection = selections.firstOrNull()
    val currentVideoStreamFormat = adaptiveTrackSelection?.selectedFormat

    val hlsMetadataEntry =
        currentVideoStreamFormat?.metadata?.iterable()
            ?.filterIsInstance(HlsTrackMetadataEntry::class.java)?.forEach {
                it.variantInfos.forEach {
                    val audioGroupId = it.audioGroupId
                    if (audioGroupId != null && audioGroupId.isNotEmpty()) {
                        audioGroupsId += audioGroupId
                    }
                }
            }

    Log.d("exophyta", "AudioGroupIds for currect selected video: $audioGroupsId")
    //assume that the first audioGroupId is what we need
    return audioGroupsId.firstOrNull()
}

internal fun Format.getHlsGroupId(): String? {
    val formatGroupIds =
        metadata?.iterable()
            ?.filterIsInstance(HlsTrackMetadataEntry::class.java)?.map {
                it.groupId
            }

    Log.d("exophyta", "Format has ${formatGroupIds?.size} count groupIds")
    //assume that the first groupId is what we need
    return formatGroupIds?.firstOrNull()
}