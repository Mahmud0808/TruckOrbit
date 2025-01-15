package com.immon.truckorbit.utils

import android.animation.ValueAnimator
import android.location.Location
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.coroutines.resume

class AnimationQueue(
    @Volatile private var startPosition: LatLng,
    private val scope: CoroutineScope,
    private val updatePosition: (LatLng) -> Unit
) {

    companion object {
        private const val ANIMATION_DURATION: Long = 600
    }

    private val items = Collections.synchronizedList(mutableListOf<LatLng>())

    fun addToQueue(newPosition: LatLng, threshold: Float) {
        if (!isSignificantChange(startPosition, newPosition, threshold)) return

        items.add(newPosition)
        if (items.size == 1) {
            scope.launch { processQueue() }
        }
    }

    private suspend fun processQueue() {
        while (items.isNotEmpty()) {
            val targetPosition = items.removeAt(0)
            animateMarker(startPosition, targetPosition)
            startPosition = targetPosition
        }
    }

    private fun isSignificantChange(
        startPosition: LatLng, newPosition: LatLng, threshold: Float
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            startPosition.latitude,
            startPosition.longitude,
            newPosition.latitude,
            newPosition.longitude,
            results
        )
        return results[0] > threshold
    }

    private suspend fun animateMarker(
        startPosition: LatLng, targetPosition: LatLng
    ) = withContext(Dispatchers.Main) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = ANIMATION_DURATION

        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val lat =
                (targetPosition.latitude - startPosition.latitude) * fraction + startPosition.latitude
            val lng =
                (targetPosition.longitude - startPosition.longitude) * fraction + startPosition.longitude
            val interpolatedPosition = LatLng(lat, lng)
            updatePosition(interpolatedPosition)
        }

        animator.doOnEnd {
            updatePosition(targetPosition)
        }

        animator.start()
        awaitAnimatorCompletion(animator)
    }

    private suspend fun awaitAnimatorCompletion(animator: ValueAnimator) {
        suspendCancellableCoroutine { continuation ->
            animator.doOnEnd {
                if (continuation.isActive) continuation.resume(Unit)
            }
            animator.doOnCancel {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }
}