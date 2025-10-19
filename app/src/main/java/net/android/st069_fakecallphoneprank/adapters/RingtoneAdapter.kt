package net.android.st069_fakecallphoneprank.adapters

import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.model.RingtoneItem

class RingtoneAdapter(
    private val ringtones: List<RingtoneItem>,
    private val onRingtoneSelected: (RingtoneItem) -> Unit
) : RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {

    private var selectedPosition = -1
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition = -1

    init {
        // Find initially selected ringtone
        selectedPosition = ringtones.indexOfFirst { it.isSelected }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ringtone, parent, false)
        return RingtoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        holder.bind(ringtones[position], position == selectedPosition, position == currentPlayingPosition)
    }

    override fun getItemCount() = ringtones.size

    fun updateSelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position

        // Notify changes
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        notifyItemChanged(position)

        onRingtoneSelected(ringtones[position])
    }

    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        val previousPlayingPosition = currentPlayingPosition
        currentPlayingPosition = -1
        if (previousPlayingPosition != -1) {
            notifyItemChanged(previousPlayingPosition)
        }
    }

    inner class RingtoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivRadio: ImageView = itemView.findViewById(R.id.ivRadio)
        private val tvRingtoneName: TextView = itemView.findViewById(R.id.tvRingtoneName)
        private val btnPlay: ImageView = itemView.findViewById(R.id.btnPlay)
        private val layoutRingtoneItem: View = itemView.findViewById(R.id.layoutRingtoneItem)

        fun bind(ringtone: RingtoneItem, isSelected: Boolean, isPlaying: Boolean) {
            tvRingtoneName.text = ringtone.name

            // Update radio button
            ivRadio.setImageResource(
                if (isSelected) R.drawable.radio_checked else R.drawable.radio_unchecked
            )

            // Update play button
            btnPlay.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause else R.drawable.ic_start_voice
            )

            // Click on item to select
            layoutRingtoneItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    updateSelection(position)
                }
            }

            // Click on play button to preview
            btnPlay.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isPlaying) {
                        // Stop playback
                        stopPlayback()
                    } else {
                        // Start playback
                        playRingtone(ringtone, position)
                    }
                }
            }
        }

        private fun playRingtone(ringtone: RingtoneItem, position: Int) {
            // Stop any current playback
            stopPlayback()

            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(itemView.context, ringtone.uri)
                    prepare()
                    start()

                    setOnCompletionListener {
                        val previousPlayingPosition = currentPlayingPosition
                        currentPlayingPosition = -1
                        if (previousPlayingPosition != -1) {
                            notifyItemChanged(previousPlayingPosition)
                        }
                    }
                }

                currentPlayingPosition = position
                notifyItemChanged(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
