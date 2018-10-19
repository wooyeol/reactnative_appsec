package com.reactnative_appsec;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainApplication extends Application implements ReactApplication {

    InputStream inputStream = null;
    FileOutputStream outputStream = null;
    BufferedInputStream bufferedInputStream = null;
    BufferedOutputStream bufferedOutputStream = null;

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }

        protected String getJSBundleFile() {
            return getExternalFilesDir(null).getAbsolutePath()+"/index.android.bundle.enc.dec";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("TEST", "onCreate");
        AssetManager assetManager = getAssets();
        byte[] key = null;

        try{
            key = generateKey("android");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try
        {
            inputStream = assetManager.open("index.android.bundle", AssetManager.ACCESS_BUFFER);
            bufferedInputStream = new BufferedInputStream(inputStream);
            outputStream = new FileOutputStream(new File(getExternalFilesDir(null), "index.android.bundle.enc"));
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            byte[] readBuffer = new byte[1024];

            int len = 0;
            while ((len = bufferedInputStream.read(readBuffer, 0, readBuffer.length)) != -1)
            {
                if(len < 1024) {
                    for(int i=len; i<1024; i++) {
                        readBuffer[i] = 0x00;
                    }
                }
                bufferedOutputStream.write(encodeFile(key, readBuffer));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bufferedInputStream.close();
                bufferedOutputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        try
        {
            inputStream = new FileInputStream(new File(getExternalFilesDir(null), "index.android.bundle.enc"));
            bufferedInputStream = new BufferedInputStream(inputStream);
            outputStream = new FileOutputStream(new File(getExternalFilesDir(null), "index.android.bundle.enc.dec"));
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            byte[] readBuffer = new byte[1024];

            int len = 0;
            while ((len = bufferedInputStream.read(readBuffer, 0, readBuffer.length)) != -1)
            {
                bufferedOutputStream.write(decodeFile(key, readBuffer));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bufferedInputStream.close();
                bufferedOutputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Log.i("TEST", "decode finish");
        SoLoader.init(this, /* native exopackage */ false);
    }

    public static byte[] generateKey(String password) throws Exception
    {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception
    {

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(fileData);

        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }
}
