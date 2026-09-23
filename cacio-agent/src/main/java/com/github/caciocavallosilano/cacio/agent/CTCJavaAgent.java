// Created by DeepSeekV3 20250823

package com.github.caciocavallosilano.cacio.agent;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import sun.misc.Unsafe;

import com.github.caciocavallosilano.cacio.ctc.*;
import com.github.caciocavallosilano.cacio.ctc.FontManagerUtil;

public class CTCJavaAgent {
    
    public static void premain(String args, Instrumentation inst) {
        try {
            // 执行原始静态初始化块中的代码
            initializeCTCEnvironment();
            
            // 设置系统属性以使用Nimbus外观
            System.setProperty("swing.defaultlaf", NimbusLookAndFeel.class.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void initializeCTCEnvironment() throws Exception {
        Field toolkit = Toolkit.class.getDeclaredField("toolkit");
        toolkit.setAccessible(true);
        toolkit.set(null, new CTCToolkit());

        Field defaultHeadlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
        defaultHeadlessField.setAccessible(true);
        defaultHeadlessField.set(null, Boolean.TRUE);
        Field headlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
        headlessField.setAccessible(true);
        headlessField.set(null, Boolean.TRUE);

        Class<?> geCls = null;
        try {
            geCls = Class.forName("java.awt.GraphicsEnvironment$LocalGE");
        } catch (Exception | NoClassDefFoundError e) {
            geCls = Class.forName("java.awt.GraphicsEnvironment");
        }
        Field ge = null;
        try {
            ge = geCls.getDeclaredField("INSTANCE");
            ge.setAccessible(true);
        } catch (Exception | NoClassDefFoundError e) {
            ge = geCls.getDeclaredField("localEnv");
            ge.setAccessible(true);
        }
        defaultHeadlessField.set(null, Boolean.FALSE);
        headlessField.set(null, Boolean.FALSE);

        Class<?> smfCls = null;
        try {
           smfCls = Class.forName("com.github.caciocavallosilano.cacio.ctc.CacioSurfaceManagerFactory"); //sun.java2d.SurfaceManagerFactory
        } catch (Exception | NoClassDefFoundError e) {
           smfCls = Class.forName("com.github.caciocavallosilano.cacio.ctc.CacioSurfaceManagerFactory");
        }

        Field smf = smfCls.getDeclaredField("instance");
        smf.setAccessible(true);
        smf.set(null, null);

        setFinalStatic(ge, new CTCGraphicsEnvironment());

        String propertyFontManager = System.getProperty("cacio.font.fontmanager");
        if (propertyFontManager != null) {
            FontManagerUtil.setFontManager(propertyFontManager);
        }
    }
    
    // 复制自原始类的setFinalStatic方法
    public static void setFinalStatic(Field field, Object value) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        Object fieldBase = unsafe.staticFieldBase(field);
        long fieldOffset = unsafe.staticFieldOffset(field);
        unsafe.putObject(fieldBase, fieldOffset, value);
    }
    
    // 复制自原始类的getFileURL方法
    static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {}

        try {
            return file.toURL();
        } catch (MalformedURLException e) {
            // Should never happen since we specify the protocol...
            throw new InternalError(e);
        }
    }
}
