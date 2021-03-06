package com.anwesh.uiprojects.litsview

/**
 * Created by anweshmishra on 15/07/18.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5

class LITSView(ctx : Context) : View(ctx) {

    val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var onCompleteListener : OnAnimationCompletionListener ?= null

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    fun addOnCompleteListener(onComplete : (Int) -> Unit) {
        onCompleteListener = OnAnimationCompletionListener(onComplete)
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun update(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }
    }

    data class LITSNode(var i : Int = 0, val state : State = State()) {

        private var prev : LITSNode? = null

        private var next : LITSNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LITSNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LITSNode {
            var curr : LITSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val size : Float = Math.min(w, h) / (nodes + 1)
            val currSize : Float = size * (i + 1) + size * this.state.scale
            paint.color = Color.parseColor("#2e3c71")
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = Math.min(w, h) / 60
            canvas.save()
            canvas.translate(w/2, h/2)
            canvas.drawLine(-currSize/2, currSize/2, currSize/2, currSize/2, paint)
            canvas.drawLine(currSize/2, currSize/2, 0F, -currSize/2, paint)
            canvas.drawLine(0f, -currSize/2, -currSize/2, currSize/2, paint)
            canvas.restore()
            val wGap : Float = w / nodes
            canvas.save()
            canvas.translate(0f, 0.9f * h)
            canvas.drawLine(i*wGap + wGap * state.scale, 0f,(i + 1) * wGap, 0f, paint)
            canvas.restore()
            next?.draw(canvas, paint)
        }
    }

    data class Lits(var i : Int) {

        private var curr : LITSNode = LITSNode(0)

        private var dir : Int = 1

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {i, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(i, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }
    }

    data class Renderer(var view : LITSView) {

        private val lits : Lits = Lits(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lits.draw(canvas, paint)
            animator.update {
                lits.update {i, it ->
                    animator.stop()
                    when (it) {
                        1f -> view.onCompleteListener?.onComplete?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            lits.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LITSView {
            val view : LITSView = LITSView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnAnimationCompletionListener(var onComplete : (Int) -> Unit)
}
