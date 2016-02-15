package es.irunet.ardudroidcontrol;

/*
 * ARDUDROIDCONTROL
 * Develop by Juan JosÈ GarcÌa
 * Rev. 2.00
 * 15-09-2014
 * 
 * Ajustado para la versiÛn 2 de Kernel.
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Service;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//sensores

public class MainActivity extends Activity {

	static final String tag = "Arduino";
	final String BOARDNAME = "SeeedBTSlave";

	// Interface con la placa arduino

	final String START2_CMD_CHAR = "@"; //Atajo para leer el canal A0
	final String START_CMD_CHAR = "*";
	final String END_CMD_CHAR = "#";
	final String DIV_CMD_CHAR = "|";
	final String CMD_DIGITALWRITE = "10";
	final String CMD_ANALOGWRITE = "11";
	final String CMD_TEXT = "12";
	final String CMD_READ_ARDUDROID = "13";
	volatile String rtaCmd;
	volatile boolean flagCmd;
	String strRtaCmd;
	// BT Variables
	private BluetoothAdapter btInterface;
	private Set<BluetoothDevice> pairedDevices;
	// private boolean bConnected = false;
	// End BT Variables
	private BluetoothSocket mBTSocket;
	static private InputStream is = null;
	// private TextView readings = null;
	static private OutputStream os = null;
	// GUI
	private Button btnConnect = null;
	private Button btnDisconnect = null;
	public TextView txSenddata = null;
	static public TextView txGetdata = null;
	private SeekBar seekBarAnalog06 = null;

	// Botones IODigitales
	private SeekBar seekBarAnalog09 = null;
	private SeekBar seekBarAnalog10 = null;
	private SeekBar seekBarAnalog11 = null;
	private Button btnIO02 = null;
	private Button btnIO03 = null;
	private Button btnIO04 = null;
	private Button btnIO05 = null;
	private Button btnIO06 = null;
	private Button btnIO07 = null;
	private Button btnIO08 = null;
	private Button btnIO09 = null;
	private Button btnIO10 = null;

	// end GUI
	private Button btnIO11 = null;
	private Button btnIO12 = null;
	private Button btnIO13 = null;
	// posicion de los cursores analogicos
	private int seekBarAnalog06Value = 0;

	// memoria salidad digitales
	private int seekBarAnalog09Value = 0;
	private int seekBarAnalog10Value = 0;
	private int seekBarAnalog11Value = 0;
	private boolean estadoSalidaDigital[] = new boolean[14]; // 0 .. 13
	static private boolean mIsBluetoothConectado = false;
	private ReadInput mReadThread = null;
	// sensor manager
	// private SensorManager sManager = null;
	// broadcast receiver to handle bt events
	private BroadcastReceiver btMonitor = null;

	public static int contador = 0;

	static final int INTERVALO_DE_ACTUALIZACION = 100;
	private Timer temporizador = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Esto asegura que la rotaci√≥n persista entre distintas actividades
		// ActivityHelper.initialize(this); //This is to ensure that the
		// rotation persists across activities and not just this one
		// Asegurar la posici√≥n vertical
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setupUI();
		setupBTMonitor();

		// sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Salidas analogicas *|11|pin|valor|#
		seekBarAnalog06
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						seekBarAnalog06Value = progress;
						String svalue = String.valueOf(seekBarAnalog06Value);

						SendCommand("*|11|6|" + svalue + "|#");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});

		seekBarAnalog09
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						seekBarAnalog09Value = progress;
						String svalue = String.valueOf(seekBarAnalog09Value);

						SendCommand("*|11|9|" + svalue + "|#");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});

		seekBarAnalog10
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						seekBarAnalog10Value = progress;
						String svalue = String.valueOf(seekBarAnalog10Value);

						SendCommand("*|11|10|" + svalue + "|#");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});

		seekBarAnalog11
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						seekBarAnalog11Value = progress;
						String svalue = String.valueOf(seekBarAnalog11Value);

						SendCommand("*|11|11|" + svalue + "|#");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_Acercade:
			Intent aboutIntent = new Intent(this, AboutArduDroidControl.class);
			startActivity(aboutIntent);
			return true;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onStop() {
		super.onStop();
		// if (sManager != null) {
		// sManager.unregisterListener(MainActivity.this);
		// }

	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(tag, "onPause");
		unregisterReceiver(btMonitor);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(tag, "onResume");

		registerReceiver(btMonitor, new IntentFilter(
				"android.bluetooth.device.action.ACL_CONNECTED"));
		registerReceiver(btMonitor, new IntentFilter(
				"android.bluetooth.device.action.ACL_DISCONNECTED"));

		// Registrar el servicio
		IntentFilter filter = new IntentFilter();
		filter.addAction(LecturaDatosIntentService.ACTION_DATO_READY);
		filter.addAction(LecturaDatosIntentService.ACTION_END);
		filter.addAction(LecturaDatosIntentService.ACTION_READ_DATA);
		ProcesarDatosReceiver rcv = new ProcesarDatosReceiver();
		registerReceiver(rcv, filter);

	}

	private void msg(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	private void setupUI() {
		txGetdata = (TextView) findViewById(R.id.txgetdata);
		txSenddata = (TextView) findViewById(R.id.txsenddata);

		btnConnect = (Button) findViewById(R.id.btnConectar);
		btnDisconnect = (Button) findViewById(R.id.btnDesConectar);
		seekBarAnalog06 = (SeekBar) findViewById(R.id.seekBarAnalog06);
		seekBarAnalog09 = (SeekBar) findViewById(R.id.seekBarAnalog09);
		seekBarAnalog10 = (SeekBar) findViewById(R.id.seekBarAnalog10);
		seekBarAnalog11 = (SeekBar) findViewById(R.id.seekBarAnalog11);

		// / btnIO digitales

		btnIO02 = (Button) findViewById(R.id.btnIO02);
		btnIO03 = (Button) findViewById(R.id.btnIO03);
		btnIO04 = (Button) findViewById(R.id.btnIO04);
		btnIO05 = (Button) findViewById(R.id.btnIO05);

		btnIO06 = (Button) findViewById(R.id.btnIO06);
		btnIO07 = (Button) findViewById(R.id.btnIO07);
		btnIO08 = (Button) findViewById(R.id.btnIO08);
		btnIO09 = (Button) findViewById(R.id.btnIO09);

		btnIO10 = (Button) findViewById(R.id.btnIO10);
		btnIO11 = (Button) findViewById(R.id.btnIO11);
		btnIO12 = (Button) findViewById(R.id.btnIO12);
		btnIO13 = (Button) findViewById(R.id.btnIO13);

		btnDisconnect.setVisibility(View.GONE);

	}

	public void findBoard(View v) {
		try {
			btInterface = BluetoothAdapter.getDefaultAdapter();
			Log.i(tag, "Local BT Interface name is [" + btInterface.getName()
					+ "]");
			pairedDevices = btInterface.getBondedDevices();
			Log.i(tag, "Found [" + pairedDevices.size() + "] devices.");
			Iterator<BluetoothDevice> it = pairedDevices.iterator();
			while (it.hasNext()) {
				BluetoothDevice bd = it.next();
				Log.i(tag, "Name of peer is [" + bd.getName() + "]");
				if (bd.getName().equalsIgnoreCase(BOARDNAME)) {
					Log.i(tag, "Found board!");
					Log.i(tag, bd.getAddress());
					Log.i(tag, bd.getBluetoothClass().toString());
					connectToBoard(bd);
					return;
				}
			}
		} catch (Exception e) {
			Log.e(tag, "Failed in findBoard() " + e.getMessage());
		}
	}

	private void connectToBoard(BluetoothDevice bd) {
		try {
			mBTSocket = bd.createRfcommSocketToServiceRecord(java.util.UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			mBTSocket.connect();
			Toast.makeText(getBaseContext(), "Bluetooth conectado",
					Toast.LENGTH_SHORT).show();
			// mIsBluetoothConectado = true;

		} catch (Exception e) {
			Log.e(tag,
					"Error interacting with remote device [" + e.getMessage()
							+ "]");
			Toast.makeText(getBaseContext(),
					"Error de conexion con equipo remoto", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// Boton desconectar
	public void disconnectFromBoard(View v) {
		// stopService(new Intent(getBaseContext(),
		// ServicioLecturaDatos.class));

		if (temporizador != null) {
			temporizador.cancel();
		}
		stopService(new Intent(getBaseContext(),
				LecturaDatosIntentService.class));

		if (mReadThread != null) {
			mReadThread.stop();

			while (mReadThread.isRunning())
				; // esperar a que finalice
			mReadThread = null;

		}
		try {
			Log.i(tag, "Intentando desconectar la conexi√≥n BT");
			mBTSocket.close();
			Toast.makeText(getBaseContext(), "Bluetooth desconectado",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Log.e(tag, "Error in DoDisconnect [" + e.getMessage() + "]");
			Toast.makeText(getBaseContext(),
					"Error es la desconexi√≥n [" + e.getMessage() + "]",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setupBTMonitor() {
		btMonitor = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						"android.bluetooth.device.action.ACL_CONNECTED")) {
					handleConnected();
				}
				if (intent.getAction().equals(
						"android.bluetooth.device.action.ACL_DISCONNECTED")) {
					handleDisconnected();
				}

			}
		};

	}

	private void handleConnected() {
		try {
			is = mBTSocket.getInputStream();
			os = mBTSocket.getOutputStream();
			// if (sManager != null) {
			// sManager.registerListener(SenseBot.this,sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
			// SensorManager.SENSOR_DELAY_UI);
			// }
			mIsBluetoothConectado = true;
			btnConnect.setVisibility(View.GONE);
			btnDisconnect.setVisibility(View.VISIBLE);

			// startService(new
			// Intent(getBaseContext(),ServicioLecturaDatos.class));
			Intent msgIntent = new Intent(MainActivity.this,
					LecturaDatosIntentService.class);
			msgIntent.putExtra(LecturaDatosIntentService.EXTRA_PARAM1, "xxx");
			startService(msgIntent);

			RefrescarDatos();
			// beginListenForData();

			// InicializarArduino();
			// msg("Placa arduino inicializada");
			// // mReadThread = new ReadInput();

		} catch (Exception e) {
			is = null;
			os = null;
			disconnectFromBoard(null);
		}

	}

	private void handleDisconnected() {

		mIsBluetoothConectado = false;

		btnConnect.setVisibility(View.VISIBLE);
		btnDisconnect.setVisibility(View.GONE);
	}

	static public synchronized void SendCommand(String comando) {
		String msg = comando;

		if (!mIsBluetoothConectado)
			return;

		// msg += "\r";
		try {
			os.write(msg.getBytes());
			os.flush();
			// Log.i(tag, "SendCommand:" + comando);
		} catch (IOException e) {

			Log.e(tag,
					"Error SendCommand:" + comando + "["
							+ e.getLocalizedMessage() + "]");
			// e.printStackTrace();
		}

	}

	// Enviar comando y esperar respuesta
	static public synchronized String SendCommandAndWaitResult(String cmd) {
		String rta = "";
		if (!mIsBluetoothConectado)
			return "9999999";
		try {
			SendCommand(cmd);
			rta = ReadResponse();

		} catch (Exception e) {
			Log.e(tag,
					"Error SendCommandAndWaitResult:" + cmd + "["
							+ e.getLocalizedMessage() + "]");
		} finally {

			return rta;
		}

	}

	/*
	// Lectura analogica A0 *|13|pin|#
	public double ReadInputAnalogica() {
		String data;
		double rta;
		int ctespera;

		if (!mIsBluetoothConectado)
			return 0.0;

		try {
			flagCmd = false;
			ctespera = 0;
			SendCommand("*|13|#");
			while (!flagCmd && ctespera < 5) {
				Thread.sleep(50);
				ctespera++;
			}
			;
			strRtaCmd = rtaCmd;

			// rta = Integer.parseInt(data);

			rta = Double.parseDouble(strRtaCmd);
			return rta;

		} catch (Exception e) {
			Log.e(tag,
					"Error LeerntradaAnalogica:" + "["
							+ e.getLocalizedMessage() + "]");
			return 0.0;

		}

	}
	*/

	// Formato *|10|pin|valor|#
	public void OnClickBotonesSalidasDigitales(View view) {
		switch (view.getId()) {
		case R.id.btnIO02:
			estadoSalidaDigital[2] = !estadoSalidaDigital[2];

			if (estadoSalidaDigital[2] == true) {
				SendCommand("*|10|2|1|#");
				btnIO02.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|2|0|#");
				btnIO02.setBackgroundColor(Color.LTGRAY);
			}
			break;

		case R.id.btnIO03:

			estadoSalidaDigital[3] = !estadoSalidaDigital[3];

			if (estadoSalidaDigital[3] == true) {
				SendCommand("*|10|3|1|#");
				btnIO03.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|3|0|#");
				btnIO03.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO04:

			estadoSalidaDigital[4] = !estadoSalidaDigital[4];

			if (estadoSalidaDigital[4] == true) {
				SendCommand("*|10|4|1|#");
				btnIO04.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|4|0|#");
				btnIO04.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO05:

			estadoSalidaDigital[5] = !estadoSalidaDigital[5];

			if (estadoSalidaDigital[5] == true) {
				SendCommand("*|10|5|1|#");
				btnIO05.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|5|0|#");
				btnIO05.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO06:

			estadoSalidaDigital[6] = !estadoSalidaDigital[6];

			if (estadoSalidaDigital[6] == true) {
				SendCommand("*|10|6|1|#");
				btnIO06.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|6|0|#");
				btnIO06.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO07:

			estadoSalidaDigital[7] = !estadoSalidaDigital[7];

			if (estadoSalidaDigital[7] == true) {
				SendCommand("*|10|7|1|#");
				btnIO07.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|7|0|#");
				btnIO07.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO08:

			estadoSalidaDigital[8] = !estadoSalidaDigital[8];

			if (estadoSalidaDigital[8] == true) {
				SendCommand("*|10|8|1|#");
				btnIO08.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|8|0|#");
				btnIO08.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO09:

			estadoSalidaDigital[9] = !estadoSalidaDigital[9];

			if (estadoSalidaDigital[9] == true) {
				SendCommand("*|10|9|1|#");
				btnIO09.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|9|0|#");
				btnIO09.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO10:

			estadoSalidaDigital[10] = !estadoSalidaDigital[10];

			if (estadoSalidaDigital[10] == true) {
				SendCommand("*|10|10|1|#");
				btnIO10.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|10|0|#");
				btnIO10.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO11:

			estadoSalidaDigital[11] = !estadoSalidaDigital[11];

			if (estadoSalidaDigital[11] == true) {
				SendCommand("*|10|11|1|#");
				btnIO11.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|11|0|#");
				btnIO11.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO12:

			estadoSalidaDigital[12] = !estadoSalidaDigital[12];

			if (estadoSalidaDigital[12] == true) {
				SendCommand("*|10|12|1|#");
				btnIO12.setBackgroundColor(Color.RED);
			} else {
				SendCommand("*|10|12|0|#");
				btnIO12.setBackgroundColor(Color.LTGRAY);
			}

			break;
		case R.id.btnIO13:

			estadoSalidaDigital[13] = !estadoSalidaDigital[13];

			if (estadoSalidaDigital[13] == true) {
				SendCommand("*|10|13|1|#");
				btnIO13.setBackgroundColor(Color.RED);

			} else {
				SendCommand("*|10|13|0|#");
				btnIO13.setBackgroundColor(Color.LTGRAY);
			}

			break;

		default:
			break;
		}
	}

	public void OnClickLecturaAnalogica(View view) {
		Intent msgIntent = new Intent(MainActivity.this,LecturaDatosIntentService.class);
		msgIntent.setAction(LecturaDatosIntentService.ACTION_READ_DATA);
		msgIntent.putExtra(LecturaDatosIntentService.EXTRA_PARAM1, "*|13|#");
		startService(msgIntent);

	}

	// Formato *|10|pin|valor|#
	public void OnClickLecturaAnalogica_ok(View view) {
		MyAsyncTask myAsyncTask = new MyAsyncTask();
		myAsyncTask.execute("*|13|#");
	}

	/*
	public void OnClickLecturaAnalogica_org(View view) {
		try {
			double rta = ReadInputAnalogica();
			// txGetdata.setText(String.valueOf(rta));
			txGetdata.setText(strRtaCmd);
		} catch (Exception e) {
			txGetdata.setText(strRtaCmd);
		}
	}
	*/

	// Poner a cero las salidas
	public void InicializarArduino() {

		try {
			SendCommand("*|10|2|0|#");
			Thread.sleep(50);
			SendCommand("*|10|3|0|#");
			Thread.sleep(50);
			SendCommand("*|10|4|0|#");
			Thread.sleep(50);
			SendCommand("*|10|5|0|#");
			Thread.sleep(50);

			SendCommand("*|10|6|0|#");
			Thread.sleep(50);
			SendCommand("*|10|7|0|#");
			Thread.sleep(50);
			SendCommand("*|10|8|0|#");
			Thread.sleep(50);
			SendCommand("*|10|9|0|#");
			Thread.sleep(50);

			SendCommand("*|10|10|0|#");
			Thread.sleep(50);
			SendCommand("*|10|11|0|#");
			Thread.sleep(50);
			SendCommand("*|10|12|0|#");
			Thread.sleep(50);
			SendCommand("*|10|13|0|#");
			Thread.sleep(50);

		} catch (Exception e) {

		}

	}

	static public synchronized String ReadResponse() {
		final byte delimiter = 10; // This is the ASCII code for a newline
									// character
		byte[] readBuffer = new byte[256];
		String data = "";
		boolean bStop = false;
		int readBufferPosition = 0;

		// InputStream inputStream;

		// contador++;
		// return String.valueOf(contador);

		try {
			// inputStream = mBTSocket.getInputStream();
			if (!mIsBluetoothConectado) {
				return "0";
			}

			while (mIsBluetoothConectado) {

				int bytesAvailable = is.available();
				if (bytesAvailable > 0) {
					byte[] packetBytes = new byte[bytesAvailable];
					is.read(packetBytes);
					for (int i = 0; i < bytesAvailable; i++) {
						byte b = packetBytes[i];
						if (b == delimiter) {
							byte[] encodedBytes = new byte[readBufferPosition];
							System.arraycopy(readBuffer, 0, encodedBytes, 0,
									encodedBytes.length);
							data = new String(encodedBytes, "US-ASCII");
							readBufferPosition = 0;
							return data;

						} else {
							readBuffer[readBufferPosition++] = b;
						}
					}
				}

				// Thread.sleep(50);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;

	}

	
	// Thread lectura respuestas de la placa
	private class ReadInput implements Runnable {
		// final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
									// character
		private byte[] readBuffer = new byte[256];
		private boolean bStop = false;
		private int readBufferPosition = 0;

		private Thread t;

		public ReadInput() {
			t = new Thread(this, "Input Thread");
			t.start();
		}

		public boolean isRunning() {

			return t.isAlive();
		}

		@Override
		public void run() {
			InputStream inputStream;

			try {
				inputStream = mBTSocket.getInputStream();
				// while (!Thread.currentThread().isInterrupted() && !bStop ) {
				while (!bStop) {

					int bytesAvailable = inputStream.available();
					if (bytesAvailable > 0) {
						byte[] packetBytes = new byte[bytesAvailable];
						inputStream.read(packetBytes);
						for (int i = 0; i < bytesAvailable; i++) {
							byte b = packetBytes[i];
							if (b == delimiter) {
								byte[] encodedBytes = new byte[readBufferPosition];
								System.arraycopy(readBuffer, 0, encodedBytes,
										0, encodedBytes.length);
								final String data = new String(encodedBytes,
										"US-ASCII");
								readBufferPosition = 0;
								rtaCmd = data;
								flagCmd = true;
								/*
								 * handler.post(new Runnable() { public void
								 * run() { //txGetdata.setText(data); rtaCmd =
								 * data; flagCmd = true; } });
								 */
							} else {
								readBuffer[readBufferPosition++] = b;
							}
						}
					}

					// Thread.sleep(50);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void stop() {

			bStop = true;
		}

	}

	
	
	
	
	private class MyAsyncTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... strings) {
			// SendCommand(strings[0]);
			// String rta = ReadResponse();
			String rta = SendCommandAndWaitResult(strings[0]);
			return rta;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// txGetdata = (TextView) findViewById(R.id.txgetdata);
			txGetdata.setText(""); // borro el campo donde va a ir el resultado
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			txGetdata.setText(s);

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

	}

	public class ProcesarDatosReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					LecturaDatosIntentService.ACTION_DATO_READY)) {
				String rta = intent.getStringExtra("DATO");
				txGetdata.setText(rta);
				// Log.d("DATO",rta);

			} else if (intent.getAction().equals(
					LecturaDatosIntentService.ACTION_END)) {
				Toast.makeText(MainActivity.this, "Tarea finalizada!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void RefrescarDatos() {
		// final MyAsyncTask myAsyncTask = new MyAsyncTask();

		temporizador.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					// Solicitar lectura canal analogico
					Intent msgIntent = new Intent(MainActivity.this,
							LecturaDatosIntentService.class);
					msgIntent
							.setAction(LecturaDatosIntentService.ACTION_READ_DATA);
					msgIntent.putExtra(LecturaDatosIntentService.EXTRA_PARAM1,
							"@"); // comando a enviar a la placa
					startService(msgIntent);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, 0, INTERVALO_DE_ACTUALIZACION);

	}
}