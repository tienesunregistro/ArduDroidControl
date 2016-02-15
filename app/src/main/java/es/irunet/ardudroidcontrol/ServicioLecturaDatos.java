package es.irunet.ardudroidcontrol;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ServicioLecturaDatos extends Service {
    int contador = 0;
    static final int INTERVALO_DE_ACTUALIZACION = 100;
    private Timer temporizador = new Timer();

    public ServicioLecturaDatos() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getBaseContext(), "Servicio iniciado", Toast.LENGTH_LONG).show();
        RefrescarDatos();
        /*
        try{
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute("*|13|#");
        } catch (Exception e){
            e.printStackTrace();
        }
        */
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (temporizador != null){
            temporizador.cancel();
        }
        Toast.makeText(getBaseContext(), "Servicio destruido", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Servicio no implementado");
    }


    private void RefrescarDatos(){
        //final MyAsyncTask myAsyncTask = new MyAsyncTask();

        temporizador.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                try{
                    MyAsyncTask myAsyncTask = new MyAsyncTask();
                    myAsyncTask.execute("*|13|#");
                   // Thread.sleep(4000);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        },0,INTERVALO_DE_ACTUALIZACION);

    }

    private class MyAsyncTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            //MainActivity.SendCommand(strings[0]);
            //String rta = MainActivity.ReadResponse();
            String rta = MainActivity.SendCommandAndWaitResult(strings[0]);
            return rta;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //txGetdata = (TextView) findViewById(R.id.txgetdata);
            //MainActivity.txGetdata.setText(""); // borro el campo donde va a ir el resultado
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            //MainActivity.txGetdata.setText(s);
            //stopSelf();
            Log.d("Servicio", s);


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }


    }

}
