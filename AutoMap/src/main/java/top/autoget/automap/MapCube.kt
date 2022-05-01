package top.autoget.automap

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class MapCube(width: Float, height: Float, depth: Float) {
    inner class CubeShader {
        var vertexShader = """precision highp float;
        attribute vec3 aVertex;//顶点数组,三维坐标
        attribute vec4 aColor;//颜色数组,三维坐标
        uniform mat4 aMVPMatrix;//mvp矩阵
        varying vec4 color;
        void main(){
            gl_Position = aMVPMatrix * vec4(aVertex, 1.0);
            color = aColor;
        }"""
        var fragmentShader = """//有颜色，没有纹理
        precision highp float;
        varying vec4 color;
        void main(){
            gl_FragColor = color;
        }"""
        var program = 0
        var aVertex = 0
        var aColor = 0
        var aMVPMatrix = 0
        val create = {
            GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).let { vertexLocation ->
                GLES20.glShaderSource(vertexLocation, vertexShader)
                GLES20.glCompileShader(vertexLocation)
                GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).let { fragmentLocation ->
                    GLES20.glShaderSource(fragmentLocation, fragmentShader)
                    GLES20.glCompileShader(fragmentLocation)
                    GLES20.glCreateProgram().let {
                        program = it
                        GLES20.glAttachShader(it, vertexLocation)
                        GLES20.glAttachShader(it, fragmentLocation)
                        GLES20.glLinkProgram(it)
                        aVertex = GLES20.glGetAttribLocation(it, "aVertex")
                        aColor = GLES20.glGetAttribLocation(it, "aColor")
                        aMVPMatrix = GLES20.glGetUniformLocation(it, "aMVPMatrix")
                    }
                }
            }
        }
    }

    var shader: CubeShader = CubeShader()
    val initShader = shader.create
    private val halfWidth: Float = width / 2
    private val halfHeight: Float = height / 2
    private val halfDepth: Float = depth / 2
    private val vertices: FloatArray = floatArrayOf(
        -halfWidth, -halfHeight, -halfDepth,
        halfWidth, -halfHeight, -halfDepth,
        halfWidth, halfHeight, -halfDepth,
        -halfWidth, halfHeight, -halfDepth,
        -halfWidth, -halfHeight, halfDepth,
        halfWidth, -halfHeight, halfDepth,
        halfWidth, halfHeight, halfDepth,
        -halfWidth, halfHeight, halfDepth
    )
    private var verticesBuffer: FloatBuffer? = ByteBuffer.allocateDirect(vertices.size * 4)
        .apply { order(ByteOrder.nativeOrder()) }.asFloatBuffer().apply {
            clear()
            put(vertices)
            position(0)
        }
    private val colors: FloatArray = floatArrayOf(
        1f, 0f, 0f, 1f,//顶点0红
        0f, 1f, 0f, 1f,//顶点1绿
        0f, 0f, 1f, 1f,//顶点2蓝
        1f, 1f, 0f, 1f,//顶点3
        0f, 1f, 1f, 1f,//顶点4
        1f, 0f, 1f, 1f,//顶点5
        0f, 0f, 0f, 1f,//顶点6
        1f, 1f, 1f, 1f
    )//顶点7
    private val colorsBuffer: FloatBuffer = ByteBuffer.allocateDirect(colors.size * 4)
        .apply { order(ByteOrder.nativeOrder()) }.asFloatBuffer().apply {
            put(colors)
            position(0)
        }
    private val indices: ShortArray = shortArrayOf(
        0, 4, 5,
        0, 5, 1,
        1, 5, 6,
        1, 6, 2,
        2, 6, 7,
        2, 7, 3,
        3, 7, 4,
        3, 4, 0,
        4, 7, 6,
        4, 6, 5,
        3, 0, 1,
        3, 1, 2
    )
    private val indicesBuffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 4)
        .apply { order(ByteOrder.nativeOrder()) }.asShortBuffer().apply {
            put(indices)
            position(0)
        }

    fun drawES20(mvp: FloatArray?) {
        GLES20.glUseProgram(shader.program)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)//启用深度测试
        GLES20.glEnableVertexAttribArray(shader.aVertex)//启用顶点属性数组
        GLES20.glVertexAttribPointer(shader.aVertex, 3, GLES20.GL_FLOAT, false, 0, verticesBuffer)
        GLES20.glEnableVertexAttribArray(shader.aColor)//启用顶点属性数组
        GLES20.glVertexAttribPointer(shader.aColor, 4, GLES20.GL_FLOAT, false, 0, colorsBuffer)
        GLES20.glUniformMatrix4fv(shader.aMVPMatrix, 1, false, mvp, 0)//4fv均匀矩阵
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indicesBuffer
        )
        GLES20.glDisableVertexAttribArray(shader.aVertex)//停用顶点属性数组
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)//停用深度测试
    }
}