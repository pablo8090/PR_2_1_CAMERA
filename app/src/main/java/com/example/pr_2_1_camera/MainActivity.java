package com.example.pr_2_1_camera;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText etSaveDialog;
    private EditText etLoadDialog;

    // Esta aplicacion te permite hacer fotos y guardarlas con un nombre, luego permite cargar fotos de local dando el nombre con el que fue guardada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSaveDialog = new EditText(this);
        etLoadDialog = new EditText(this);

        // Boton foto
        Button btPhoto = findViewById(R.id.btPhoto);
        btPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre un intent a la camara para sacar una foto
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,REQUEST_IMAGE_CAPTURE);
            }
        });


        // Boton cargar imagen
        Button btLoad = findViewById(R.id.btLoad);
        btLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Muestra el dialogo de carga
                showLoadDialog();
            }
        });
    }


    // Esta funcion setea un bitmap a la ImageView de la aplicacion
    public void setImageView(Bitmap bitmap)
    {
        ImageView iv = findViewById(R.id.imageView);
        iv.setImageBitmap(bitmap);
    }


    // Esta funcion es la que se lanza cuando acaba la actividad del intent de la camara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Si la imagen ha salido bien y ha sido devuelta extraemos el bitmap
            Bundle extras = data.getExtras();
            Bitmap bp = (Bitmap)extras.get("data");

            // Cambia el bitmap de la imageView
            setImageView(bp);

            // Muestra el dialogo de guardar imagen, le paso el bitmap para guardarlo
            showSaveDialog(bp);
        }
    }


    // Permite construir un dialogo con el positiveButton vacio por parametros
    private AlertDialog getDialog(String title, String message, EditText et, String cancelMessage) {
        // Builds ranking dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setView(et);

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Vacío porque luego se hace override despues del show()
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (cancelMessage != null)
                {
                    Toast.makeText(getApplicationContext(),cancelMessage, Toast.LENGTH_LONG).show();
                }

            }
        });

        AlertDialog ad = adb.create();
        ad.setCanceledOnTouchOutside(false);
        return ad;

    }


    // Esta funcion muestra y configura el dialogo de guardar imagen
    private void showSaveDialog(Bitmap bp)
    {
        // crea un nuevo edit text
        etSaveDialog = new EditText(this);
        // Construyo un dialog y lo muestro
        AlertDialog ad = getDialog("¿Quieres guardar tu foto?", "Introduce el nombre de la foto: ", etSaveDialog,"No se ha guardado la foto!");
        ad.show();

        // Sobreescribo el OnClickListener del boton "OK" del dialogo (permite hacer el ad.dismiss() cuando quieras, controlando el input)
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String value =  etSaveDialog.getText().toString();

                if ((value.length() <= 10 && value.length() >= 1) && !value.contains("\n") && !value.contains(" "))
                {
                    // Si el input es correcto guardamos el bitmap con el nombre dado
                    if (saveImage(bp, value)) // el return booleano de saveImage() nos indica si el nombre dado existia ya o no, para saber si cerrar el dialogo
                    {
                        // Si el nombre de la imagen no existia previamente, podemos cerrar el dialogo
                        ad.dismiss();
                    }
                }
                else
                {
                    // Si el input no es correcto (1-10 caracteres sin espacios) muestra un toast de error
                    Toast t = Toast.makeText(getApplicationContext(), "El nombre tiene que contener entre 1 y 10 caracteres y sin espacios", Toast.LENGTH_LONG);
                    t.show();
                }
            }
        });

    }

    // Esta funcion muestra y configura el dialogo de cargar una imagen
    private void showLoadDialog()
    {
        // crea un nuevo edit text
        etLoadDialog = new EditText(this);
        // Construyo un dialog y lo muestro
        AlertDialog ad = getDialog("Cargar Foto de las almacenadas", "Introduce el nombre de la foto: ", etLoadDialog,null);
        ad.show();

        // Sobreescribo el OnClickListener del boton "OK" del dialogo (permite hacer el ad.dismiss() cuando quieras, controlando el input)
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String value =  etLoadDialog.getText().toString();
                if ((value.length() <= 10 && value.length() >= 1) && !value.contains("\n") && !value.contains(" "))
                {
                    // Si el input es correcto llamo a la funcion loadImage con el nombre dado
                    try {
                        loadImage(value);
                        Toast.makeText(getApplicationContext(), "Foto cargada correctamente con el nombre " + value, Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast t = Toast.makeText(getApplicationContext(), "ERROR: Error al importar la imagen, el nombre dado no existe",Toast.LENGTH_LONG);
                        t.show();
                    }
                    ad.dismiss();

                }
                else
                {
                    Toast t = Toast.makeText(getApplicationContext(), "El nombre tiene que contener entre 1 y 10 caracteres y sin espacios", Toast.LENGTH_LONG);
                    t.show();
                }
            }
        });
    }


    // Esta funcion guarda el bitmap en caso de que no exista una imagen ya con ese nombre.
    public boolean saveImage(Bitmap bp, String name)
    {

        File imageFile = new File(getApplicationContext().getFilesDir(), name + ".png");
        if (!imageFile.exists())
        {
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                bp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(getApplicationContext(), "Foto guardada correctamente con el nombre " + name, Toast.LENGTH_LONG).show();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "ERROR: Ha habido un error al guardar la imagen", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Ya existe una imagen con ese nombre, introduce otro!", Toast.LENGTH_LONG).show();
            return false;
        }

    }


    // Esta funcion carga una imagen de local dado un nombre, y la setea a la imageView de la aplicacion
    private void loadImage(String imageName) throws FileNotFoundException {
        ImageView iv = findViewById(R.id.imageView);
        // utiliza la funcion getBitmap() para cargarlo de local
        iv.setImageBitmap(getBitmap(imageName + ".png", getApplicationContext().getFilesDir()));
    }


    // Esta funcion devuelve un bitmap de local dado un nombre de fichero y una ruta
    private Bitmap getBitmap(String filename, File dirPath) throws FileNotFoundException {
        File bFile = new File(dirPath.getAbsolutePath(), filename);
        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(bFile));
        return b;
    }

}