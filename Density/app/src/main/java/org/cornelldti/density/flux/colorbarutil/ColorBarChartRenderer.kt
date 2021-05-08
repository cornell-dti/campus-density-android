package org.cornelldti.density.flux.colorbarutil

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF

import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.ceil

class ColorBarChartRenderer(chart: BarDataProvider, animator: ChartAnimator,
                            viewPortHandler: ViewPortHandler) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val path = Path()
    private val mBarShadowRectBuffer = RectF()
    private val radii = floatArrayOf(
            7f, 7f, // top left
            7f, 7f, // top right
            0f, 0f, // bottom right
            0f, 0f // bottom left
    )


    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {

        val trans = mChart.getTransformer(dataSet.axisDependency)

        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled) {
            mShadowPaint.color = dataSet.barShadowColor

            val barData = mChart.barData

            val barWidth = barData.barWidth
            val barWidthHalf = barWidth / 2.0f
            var x: Float

            val count = Math.min(ceil((dataSet.entryCount.toFloat() * phaseX).toDouble()).toInt(), dataSet.entryCount)
            for (i in 0 until count) {
                val e = dataSet.getEntryForIndex(i)

                x = e.x

                mBarShadowRectBuffer.left = x - barWidthHalf
                mBarShadowRectBuffer.right = x + barWidthHalf

                trans.rectValueToPixel(mBarShadowRectBuffer)

                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
                    continue
                }

                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                    break

                mBarShadowRectBuffer.top = mViewPortHandler.contentTop()
                mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom()

                c.drawRoundRect(mBarShadowRectBuffer, 7f, 7f, mShadowPaint)
            }
        }

        // initialize the buffer
        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)

        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        val isSingleColor = dataSet.colors.size == 1

        if (isSingleColor) {
            mRenderPaint.color = dataSet.color
        }

        for (j in 0 until buffer.size() step 4) {

            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                continue
            }

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                break

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.color = dataSet.getColor(j / 4)
            }

            if (mRenderPaint.color == dataSet.colors[4]) {
                c.drawRect(buffer.buffer[j],
                        50f,
                        buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint)
            } else {
                path.reset()
                path.addRoundRect(
                        buffer.buffer[j] + 2,
                        buffer.buffer[j + 1],
                        buffer.buffer[j + 2] - 2,
                        buffer.buffer[j + 3],
                        radii,
                        Path.Direction.CW)
                c.drawPath(path, mRenderPaint)
            }

            if (drawBorder) {
                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], 7f, 7f, mBarBorderPaint)
            }
        }
    }
}
