package com.winhands.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.View;

/**
 * 本地图片缓存加载
 */
public class NativeImageLoader implements ImageCache {
	private LruCache<String, Bitmap> mLruCache;
	private static NativeImageLoader mInstance;
	private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(5);

	private NativeImageLoader() {
		// 获取应用程序的最大内存
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// 用最大内存的1/8来缓存图片
		final int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			// 获取每张图片的大小
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
			}
		};
	}

	/**
	 * 获取NativeImageLoader的实例
	 */
	public static NativeImageLoader getInstance() {
		if (mInstance == null) {
			L.i("实例化NativeImageLoader");
			mInstance = new NativeImageLoader();
		}
		return mInstance;
	}

	public Bitmap loadNativeImage(String path, NativeImageCallBack mCallBack) {
		return loadNativeImage(path, mCallBack, 100, 100, null);
	}

	public Bitmap loadNativeImage(String path, NativeImageCallBack mCallBack, View view) {
		return loadNativeImage(path, mCallBack, 100, 100, view);
	}

	/**
	 * 此方法来加载本地图片
	 */
	public Bitmap loadNativeImage(final String path, final NativeImageCallBack mCallBack, final int width,
			final int height, final View view) {
		// 先获取内存中的Bitmap
		Bitmap bitmap = getBitmapFromMemCache(path);
		final Handler mHander = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				// super.handleMessage(msg);
				mCallBack.onImageLoader((Bitmap) msg.obj, path);
				if (view != null) {
					mCallBack.onImageLoader((Bitmap) msg.obj, view);
				}
				return false;
			}
		});

		// 若该Bitmap不在内存缓存中，则启用线程去加载本地的图片，并将Bitmap加入到mMemoryCache中
		if (bitmap == null) {
			mImageThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					// 先获取图片的缩略图
					Bitmap mBitmap = ImageUtils.getImageThumbnail(path, width, height);
					Message msg = mHander.obtainMessage();
					msg.obj = mBitmap;
					mHander.sendMessage(msg);
					// 将图片加入到内存缓存
					addBitmapToMemoryCache(path, mBitmap);
				}
			});
		}
		return bitmap;
	}

	/**
	 * 往内存缓存中添加Bitmap
	 */
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && bitmap != null) {
			mLruCache.put(key, bitmap);
		}
	}

	/**
	 * 根据key来获取内存中的图片
	 */
	private Bitmap getBitmapFromMemCache(String key) {
		return mLruCache.get(key);
	}

	/**
	 * 加载本地图片的回调接口
	 */
	public interface NativeImageCallBack {
		/**
		 * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
		 */
		public void onImageLoader(Bitmap bitmap, String path);

		public void onImageLoader(Bitmap bitmap, View view);

	}

	@Override
	public Bitmap getBitmap(String url) {
		return getBitmapFromMemCache(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		addBitmapToMemoryCache(url, bitmap);
	}
}