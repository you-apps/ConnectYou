package com.bnyro.contacts.presentation.components.shapes

import androidx.compose.runtime.Composable
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon

@Composable
fun BubbleShape() = RoundedPolygonShape(
    RoundedPolygon(
        numVertices = 4,
        perVertexRounding = listOf(
            CornerRounding(
                10f,
            ),
            CornerRounding(
                2f,
            ),
            CornerRounding(
                10f,
            ),
            CornerRounding(
                10f,
            ),
        ),
    )
) { matrix -> matrix.rotateZ(-45f) }