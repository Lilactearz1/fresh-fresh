package com.movix.transak_infield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.nio.file.Files;

public class Templatepdf1{
    //create a typface resources

    public Typeface getTypeface(Context context) {
       Typeface typeface=ResourcesCompat.getFont(context,R.font.queen);
        return typeface;
    }


    // Method to generate QR code and return iText 7 Image
    public Image qrcodeGenerator(String data) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 106, 106);
            Bitmap bitmap = Bitmap.createBitmap(110, 110, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < 105; x++) {
                for (int y = 0; y < 105; y++) {
                    boolean isSet = bitMatrix.get(x, y);
                    int color = isSet ? Color.BLACK : Color.TRANSPARENT;
                    bitmap.setPixel(x, y, color);
                }
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(byteArray);
            return new Image(imageData);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image barCodeGenerator(String data) {
        try {
            // barcode dimensions
            int width = 200;
            int height = 40;

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(
                    data,
                    BarcodeFormat.PDF_417,
                    width,
                    height
            );

            // ZXing internal quiet zone is applied automatically
            int matrixWidth = bitMatrix.getWidth();
            int matrixHeight = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < matrixWidth; x++) {
                for (int y = 0; y < matrixHeight; y++) {
                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE
                    );
                }
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();

            ImageData imageData = ImageDataFactory.create(bytes);
            return new Image(imageData);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void ePdf(Context context) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(720, 1120, 2).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(24);


        paint.setColor(Color.rgb(22, 33, 44));
        canvas.drawText("We did it Bro and we gonna still do it bro", 50, 40, paint);

        pdfDocument.finishPage(page);

        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Documents", "Didit.pdf");

        try {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pdfDocument.writeTo(Files.newOutputStream(file.toPath()));
            }
            pdfDocument.close();

            Toast.makeText(context, "downloaded estimate", Toast.LENGTH_SHORT).show();
            System.out.println("downloaded");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
