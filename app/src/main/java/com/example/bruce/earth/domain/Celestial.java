package com.example.bruce.earth.domain;

import android.content.Context;
import android.opengl.GLES20;

import com.example.bruce.earth.util.MatrixState;
import com.example.bruce.earth.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Celestial {	//表示星空天球的类
    private FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
    private int vCount=0;//星星数量
    private float scale;//星星尺寸
    private int mProgram;//自定义渲染管线程序id
    private int muMVPMatrixHandle;//总变换矩阵引用id
    private int maPositionHandle; //顶点位置属性引用id
    private int uPointSizeHandle;//顶点尺寸参数引用
    public Celestial(Context context,float scale, float yAngle, int vCount){
//        this.yAngle=yAngle;
        this.scale=scale;
        this.vCount=vCount;
        initVertexData();
        intShader(context);
    }
    private void initVertexData(){ //初始化顶点坐标的方法
        //顶点坐标数据的初始化
        float vertices[]=new float[vCount*3];
        for(int i=0;i<vCount;i++){
            //随机产生每个星星的xyz坐标
            double angleTempJD=Math.PI*2*Math.random();
            double angleTempWD=Math.PI*(Math.random()-0.5f);
            float UNIT_SIZE = 10.0f;
            vertices[i*3]=(float)(UNIT_SIZE *Math.cos(angleTempWD)*Math.sin(angleTempJD));
            vertices[i*3+1]=(float)(UNIT_SIZE *Math.sin(angleTempWD));
            vertices[i*3+2]=(float)(UNIT_SIZE *Math.cos(angleTempWD)*Math.cos(angleTempJD));
        }
        //创建顶点坐标数据缓冲
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为int型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
    }
    private void intShader(Context mv){    //初始化着色器
        //加载顶点着色器的脚本内容
        String mVertexShader = ShaderUtil.loadFromAssetsFile("vertex_xk.sh", mv.getResources());
        ShaderUtil.checkGlError("==ss==");
        //加载片元着色器的脚本内容
        String mFragmentShader = ShaderUtil.loadFromAssetsFile("frag_xk.sh", mv.getResources());
        //基于顶点着色器与片元着色器创建程序
        ShaderUtil.checkGlError("==ss==");
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取顶点尺寸参数引用
        uPointSizeHandle = GLES20.glGetUniformLocation(mProgram, "uPointSize");
    }
    public void drawSelf(){
        GLES20.glUseProgram(mProgram); //制定使用某套着色器程序
        //将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        GLES20.glUniform1f(uPointSizeHandle, scale);  //将顶点尺寸传入着色器程序
        GLES20.glVertexAttribPointer( //为画笔指定顶点位置数据
                maPositionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                3*4,
                mVertexBuffer
        );
        //允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vCount); //绘制星星点
    }

}
