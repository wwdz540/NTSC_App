package com.winhands.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

public class ImageUtils {
	public static final long MAX_CODE_LENGTH = 200 * 1024;
	public static final long MAX_COMPRESS_LENGTH = 200 * 1024;

	public static Bitmap scaleImage(String imagePath, int requestWidth, int requestHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		options.inSampleSize = calculateInSampleSize(options, requestWidth, requestHeight);

		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

		String orientation = getExifOrientation(imagePath, "0");

		Matrix matrix = new Matrix();
		matrix.postRotate(Float.valueOf(orientation));

		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

		return newBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqW, int reqH) {
		final int h = options.outHeight;
		final int w = options.outWidth;
		int inSampleSize = 1;

		if (h > reqH || w > reqW) {
			final int heightRatio = Math.round((float) h / (float) reqH);
			final int widthRatio = Math.round((float) w / (float) reqW);

			inSampleSize = Math.min(heightRatio, widthRatio);
		}

		return inSampleSize;
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处：
	 * 1.使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	 * 2.缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth > beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);

		Bitmap newBitmap = null;

		if (bitmap != null) {
			String orientation = getExifOrientation(imagePath, "0");

			Matrix matrix = new Matrix();
			matrix.postRotate(Float.valueOf(orientation));

			newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		}

		return newBitmap;

		// if (newBitmap != null) {
		// // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		// newBitmap = ThumbnailUtils.extractThumbnail(newBitmap, width, height,
		// ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		// return newBitmap;
		// } else {
		// return null;
		// }

	}

	/**
	 * @param tempDirPath
	 *            项目内 临时图片文件处理目录
	 * @param originalPath
	 *            待处理(压缩、扶正)的本地图片文件路径
	 * @return 当原文件不存在 返回null 当处理失败 均返回原图片文件路径 当处理成功 返回处理后新图片路径
	 */
	public static String handleLocalBitmapFile(String originalPath, String tempDirPath) {
		File originalFile = new File(originalPath);
		String originalFileName = originalFile.getName();
		long originalSize = originalFile.length();
		if (!originalFile.exists() || originalSize == 0) {
			L.e("图片文件不存在");
			return null;
		}
		String orientation = getExifOrientation(originalPath, "0");
		if ("0".equals(orientation)) {
			Bitmap resultBitmap = condensationBitmapFile(originalFile, originalSize, MAX_CODE_LENGTH);
			if (resultBitmap == null) {
				return originalPath;
			} else {
				String aimPath = createTempFileName(originalFileName, tempDirPath);
				if (writeImageFile(aimPath, resultBitmap)) {
					return aimPath;
				} else {
					return originalPath;
				}
			}
		} else {
			Bitmap standedBitmap = standBitmap(orientation,
					condensationBitmapFile(originalFile, originalSize, MAX_COMPRESS_LENGTH));
			if (standedBitmap == null) {
				return originalPath;
			} else {
				String standedPath = createTempFileName(originalFileName, tempDirPath);
				if (writeImageFile(standedPath, standedBitmap)) {
					return standedPath;
				} else {
					return originalPath;
				}
			}
		}
	}

	private static Bitmap condensationBitmapFile(File originalFile, long originalSize, long aimSize) {
		FileInputStream fileinputstream = null;
		Bitmap bitmap = null;
		// 计算缩放比
		int be = 1;
		while (originalSize > aimSize) {
			originalSize /= 4;
			be *= 2;
		}
		try {
			fileinputstream = new FileInputStream(originalFile);
			FileDescriptor filedescriptor = fileinputstream.getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inJustDecodeBounds = false;
			L.e("压缩比例为" + be);
			options.inSampleSize = be;
			// 重新读入图片，注意这次要把options.inJustDecodeBounds设为false
			bitmap = BitmapFactory.decodeFileDescriptor(filedescriptor, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileinputstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	private static Bitmap standBitmap(String orientation, Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		Matrix matrix = new Matrix();
		matrix.postRotate(Float.valueOf(orientation));
		Bitmap resultBitmap = null;
		try {
			resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			return bitmap;
			// 如何出现了内存不足异常，最好return 原始的bitmap对象。.
		}
		if (resultBitmap != null) {
			return resultBitmap;
		} else {
			return bitmap;
		}
	}

	private static boolean writeImageFile(String aimPath, Bitmap bitmap) {
		if (bitmap == null) {
			return false;
		}
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(aimPath);
			bos = new BufferedOutputStream(fos);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String createTempFileName(String originalFileName, String tempDirPath) {
		File aimFile = new File(tempDirPath, "temp_" + originalFileName);
		return aimFile.getAbsolutePath();
	}

	public static String getExifOrientation(String path, String orientation) {
		Method exif_getAttribute;
		Constructor<ExifInterface> exif_construct;
		String exifOrientation = "";

		int sdk_int = 0;
		try {
			sdk_int = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (Exception e1) {
			sdk_int = 3; // assume they are on cupcake
		}
		if (sdk_int >= 5) {
			try {
				exif_construct = android.media.ExifInterface.class.getConstructor(new Class[] { String.class });
				Object exif = exif_construct.newInstance(path);
				exif_getAttribute = android.media.ExifInterface.class.getMethod("getAttribute",
						new Class[] { String.class });
				try {
					exifOrientation = (String) exif_getAttribute.invoke(exif,
							android.media.ExifInterface.TAG_ORIENTATION);
					if (exifOrientation != null) {
						if (exifOrientation.equals("1")) {
							orientation = "0";
						} else if (exifOrientation.equals("3")) {
							orientation = "180";
						} else if (exifOrientation.equals("6")) {
							orientation = "90";
						} else if (exifOrientation.equals("8")) {
							orientation = "270";
						}
					} else {
						orientation = "0";
					}
				} catch (InvocationTargetException ite) {
					/* unpack original exception when possible */
					orientation = "0";
				} catch (IllegalAccessException ie) {
					System.err.println("unexpected " + ie);
					orientation = "0";
				}
				/* success, this is a newer device */
			} catch (NoSuchMethodException nsme) {
				orientation = "0";
			} catch (IllegalArgumentException e) {
				orientation = "0";
			} catch (InstantiationException e) {
				orientation = "0";
			} catch (IllegalAccessException e) {
				orientation = "0";
			} catch (InvocationTargetException e) {
				orientation = "0";
			}

		}
		return orientation;
	}

	// 裁剪图片
	// public static void cropImage(Object activity, Uri uri, int outputX, int
	// outputY, int requestCode) {
	// cropImage(activity, uri, outputX, outputY, requestCode, "JPEG");
	// }

	public static void cropImage(Object activity, Uri uri, int outputX, int outputY, int requestCode,
			String outputFormat) {
		// Intent intent = new Intent("com.android.camera.action.CROP");
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.setClassName("com.android.camera", "com.android.camera.CropImage");
		// intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setDataAndType(uri, "image/*");
		// intent.putExtra("crop", true );
		// intent.putExtra("aspectX", 1);
		// intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", outputX);
		// intent.putExtra("outputY", outputY);
		// intent.putExtra("scale", true);
		// intent.putExtra("outputFormat", outputFormat);
		// intent.putExtra("noFaceDetection", true);
		// intent.putExtra("return-data", false); // 为节省内存，不返回bitmap
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		((Fragment) activity).startActivityForResult(intent, requestCode);

		// Intent intent1 = new Intent(this, CropImage.class);
		// intent.putExtra(CropImage.IMAGE_PATH, path);
		//
		// intent.putExtra(CropImage.SCALE, true);
		//
		// intent.putExtra("aspectX", aspectX);
		// intent.putExtra("aspectY", aspectY);
		//
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
		// File(mCurrentPhotoPath)));
		//
		// startActivityForResult(intent, CROP);

	}
}