package com.example.filetest;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import weka.core.Check;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

/**
 * Created by alexd on 2017/10/9.
 */

public class MainActivity extends Activity {

	private Button mButton;
	private EditText et;
	private TextView resultTv;
	private CheckBox c1;
	private CheckBox c2;
	private CheckBox c3;
	private CheckBox cDTPara;
	private CheckBox cDTBinarySplit;
	private CheckBox cLRPara;
	private EditText etDTConfidenceFactor;
	private EditText etLRRidge;

	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private static final String [] dataset ={"iris", "breast-cancer", "credit-g", "glass", "hypothyroid"/*, "supermarket"*/};
	private static final String IP = "10.143.10.253";
	static final String PROJECT_PATH = "sdcard/Benchmarking_ML_Project/";
	static final String LOGFILE_PATH = "mylog.txt";
	private String chooseDataset = "iris";
	private String text = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		c1 = (CheckBox) findViewById(R.id.checkBox1);
		c2 = (CheckBox) findViewById(R.id.checkBox2);
		c3 = (CheckBox) findViewById(R.id.checkBox3);
		cDTPara = (CheckBox) findViewById(R.id.checkBoxDT);
		cDTBinarySplit = (CheckBox) findViewById(R.id.checkBoxDTSplit);
		cLRPara = (CheckBox) findViewById(R.id.checkBoxLR);

		spinner = (Spinner) findViewById(R.id.spinner);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dataset);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setVisibility(View.VISIBLE);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
											  @Override
											  public void onItemSelected(AdapterView<?> arg0, View arg1,
																		 int arg2, long arg3) {
												  // TODO Auto-generated method stub
												  chooseDataset = (((TextView) arg1).getText()).toString();
											  }
												@Override
												public void onNothingSelected(AdapterView<?> arg0) {
													// TODO Auto-generated method stub

												}
											});
		et = (EditText)findViewById(R.id.edtPercent);
		etDTConfidenceFactor = (EditText)findViewById(R.id.edtDTConfidenceFactor);
		etLRRidge = (EditText)findViewById(R.id.edtLRRidge);
//        et.setText("80");

		etDTConfidenceFactor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		resultTv = (TextView) findViewById(R.id.TextViewResult);
		resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
		mButton = (Button)findViewById(R.id.btnexec);

		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0){
				// TODO Auto-generated method stub
				resultTv.setText("Please wait...");
				logAction("Execute Button Clicked");
				String alg = getChoices();
				String rate = et.getText().toString();
				if (!inputCheck(alg, rate)){
					resultTv.setText("Please check your input");
					logAction("Input Check Failed");
					return;
				}
				double splitRate = Double.valueOf(rate);
				FileTransferTask fileTransferTask = new FileTransferTask();
				Toast.makeText(getApplicationContext(), "Executing",Toast.LENGTH_SHORT).show();
				fileTransferTask.execute(new TaskParams(alg, splitRate, chooseDataset));
//				Toast.makeText(getApplicationContext(), text,Toast.LENGTH_SHORT).show();	alg = et.getText().toString();
			}
		});
	
   }

   private boolean inputCheck(String alg, String rate){
	   if (alg.equals("") || alg.length() == 0){
		   Toast.makeText(getApplicationContext(), "Please select your algorithm",Toast.LENGTH_SHORT).show();
		   return false;
	   }
	   if (rate == null || rate.equals("") || rate.length() == 0){
		   Toast.makeText(getApplicationContext(), "Please enter split rate",Toast.LENGTH_SHORT).show();
		   return false;
	   }
	   double splitRate = Double.valueOf(rate);
	   if (splitRate > 99 || splitRate < 1){
		   Toast.makeText(getApplicationContext(), "Split rate must be between 1 and 99",Toast.LENGTH_SHORT).show();
		   return false;
	   }
	   if (c2.isChecked() && cDTPara.isChecked()){
		   String confidenceFactor = etDTConfidenceFactor.getText().toString();
		   if (confidenceFactor == null || confidenceFactor.equals("") || confidenceFactor.length() == 0) {
		   } else {
			   float f_confidenceFactor = Float.parseFloat(confidenceFactor);
			   if (f_confidenceFactor < 0.01 || f_confidenceFactor > 0.5){
				   Toast.makeText(getApplicationContext(), "Confidence Factor must be between 0.01 and 0.5",Toast.LENGTH_SHORT).show();
				   return false;
			   }
		   }
	   }
	   return true;
   }

	private String getChoices(){
		StringBuilder sb = new StringBuilder();
		String dilimiter = "";
		if (c1.isChecked()){
			sb.append(dilimiter);
			dilimiter = "|";
			sb.append("nb");
		}
		if (c2.isChecked()){
			sb.append(dilimiter);
			dilimiter = "|";
			sb.append("dt");
			if (cDTPara.isChecked()){
				String confidenceFactor = etDTConfidenceFactor.getText().toString();
				if (confidenceFactor == null || confidenceFactor.equals("") || confidenceFactor.length() == 0) {
					sb.append(":");
					sb.append("0");
				} else {
					float f_confidenceFactor = Float.parseFloat(confidenceFactor);
					sb.append(":");
					sb.append(f_confidenceFactor);
				}
				String binary_split = cDTBinarySplit.isChecked()?"true":"false";
				sb.append(",");
				sb.append(binary_split);
			}
		}
		if (c3.isChecked()){
			sb.append(dilimiter);
			sb.append("lr");
			if (cLRPara.isChecked()){
				String ridge = etLRRidge.getText().toString();
				if (ridge == null || ridge.equals("") || ridge.length() == 0) {
				} else {
					double d_ridge = Double.parseDouble(ridge);
					sb.append(":");
					sb.append(d_ridge);
				}
			}
		}
		return sb.toString();
	}

	private void sendData(SocketChannel socketChannel, String data, boolean shutdown) throws IOException {
		byte[] bytes = data.getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		socketChannel.write(buffer);
		if (shutdown) {
			socketChannel.socket().shutdownOutput();
		} else {
			socketChannel.socket().getOutputStream().flush();
		}
	}

	private String receiveData(SocketChannel socketChannel) throws IOException {
		String data = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			byte[] bytes;
			int count = 0;
			while ((count = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[count];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}
			bytes = baos.toByteArray();
			data = new String(bytes);
	//				socketChannel.socket().shutdownInput();
		} finally {
			try {
				baos.close();
			} catch(Exception ex) {}
		}
		return data;
	}

	private static void sendFile(SocketChannel socketChannel, File file) throws IOException {
		FileInputStream fis = null;
		FileChannel channel = null;
		try {
			fis = new FileInputStream(file);
			channel = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			int size = 0;
			while ((size = channel.read(buffer)) != -1) {
				buffer.rewind();
				buffer.limit(size);
				socketChannel.write(buffer);
				buffer.clear();
			}
			socketChannel.socket().shutdownOutput();
		} finally {
			try {
				channel.close();
			} catch(Exception ex) {}
			try {
				fis.close();
			} catch(Exception ex) {}
		}
	}

	private static void receiveFile(SocketChannel socketChannel, File file) throws IOException {
		FileOutputStream fos = null;
		FileChannel channel = null;
		if(!file.exists()){
			file.createNewFile();
		}

		try {
			fos = new FileOutputStream(file);
			channel = fos.getChannel();
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

			int size = 0;
			while ((size = socketChannel.read(buffer)) != -1) {
				buffer.flip();
				if (size > 0) {
					buffer.limit(size);
					channel.write(buffer);
					buffer.clear();
				}
			}
		} finally {
			try {
				channel.close();
			} catch(Exception ex) {}
			try {
				fos.close();
			} catch(Exception ex) {}
		}
	}

	private void logAction(String action){
		File file = new File(PROJECT_PATH + LOGFILE_PATH);
		FileWriter fw = null;
		BufferedWriter bw = null;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String sd = sdf.format(new Date(date.getTime()));
		String content = "Action " + action + " complete on: " + sd +"\n";
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			fw=new FileWriter(file.getAbsoluteFile(),true);  //true means append to end
			bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			fw.close();
		} catch (Exception e){
			Toast.makeText(getApplicationContext(), "Error while logging action",Toast.LENGTH_SHORT).show();
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.log) {
			Intent intent = new Intent(MainActivity.this, ViewLogActivity.class);
			startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	class TaskParams {
		String alg;
		double splitRate;
		String choosedDataset;

		public TaskParams(String alg, double splitRate, String choosedDataset) {
			this.alg = alg;
			this.splitRate = splitRate;
			this.choosedDataset = choosedDataset;
		}

		public String getAlg() {
			return alg;
		}

		public double getSplitRate() {
			return splitRate;
		}


		public String getChoosedDataset() {
			return choosedDataset;
		}

	}

	private class FileTransferTask extends AsyncTask<TaskParams, Void, String> {
		StringBuilder mysb = new StringBuilder();
		private Lock lock = new ReentrantLock();

		@Override
		protected String doInBackground(TaskParams... taskParamses) {
			String alg = taskParamses[0].getAlg();
			double splitRatio = taskParamses[0].getSplitRate() / 100;
			String choosedDataset = taskParamses[0].getChoosedDataset();
			String datapath = "iris";		//if (choosedDataset.equals(dataset[0]))
			if (choosedDataset.equals(dataset[1])) {
				datapath = "breast-cancer";
			} else if (choosedDataset.equals(dataset[2])) {
				datapath = "credit-g";
			} else if (choosedDataset.equals(dataset[3])) {
				datapath = "glass";
			} else if (choosedDataset.equals(dataset[4])) {
				datapath = "hypothyroid";
			}/* else if (choosedDataset.equals(dataset[5])) {
				datapath = "supermarket";
			}*/
			String result = "";
			SocketChannel socketChannel = null;
					try {
						// load data
						String dataPath = PROJECT_PATH + "data/" + datapath + ".arff";
						Instances data = new Instances(new BufferedReader(new FileReader(dataPath)));
						data.setClassIndex(data.numAttributes() - 1);
						// split into train and test
						data.randomize(new java.util.Random());
						int trainSize = Math.min(data.numInstances() - 1, Math.max(1, (int) Math.round(data.numInstances() * splitRatio)));
						int testSize = data.numInstances() - trainSize;
						Instances train = new Instances(data, 0, trainSize);
						Instances test = new Instances(data, trainSize, testSize);

						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROJECT_PATH + "trainData"));
						oos.writeObject(train);
						oos.flush();
						oos.close();

						socketChannel = SocketChannel.open();
						SocketAddress socketAddress = new InetSocketAddress(IP, 1991);
						socketChannel.connect(socketAddress);

						// send training data file to server
						File traindata = new File(PROJECT_PATH + "trainData");
						sendData(socketChannel, "sending", false);
						String reveivestatus = receiveData(socketChannel);
						if (reveivestatus.equals("receiving")) {
							sendFile(socketChannel,traindata);
							logAction("Send Training Data to Server");

							// send selected algorithm to server
							socketChannel = SocketChannel.open();
							socketChannel.connect(new InetSocketAddress(IP, 1991));
							sendData(socketChannel, alg, true);
							String modelFileName = "";
							modelFileName = receiveData(socketChannel);
							if (!modelFileName.isEmpty()) {
								socketChannel = SocketChannel.open();
								socketChannel.connect(new InetSocketAddress(IP, 1991));
								sendData(socketChannel, modelFileName, true);
								logAction("Receive Data Models from Server");
								receiveFile(socketChannel, new File(PROJECT_PATH + modelFileName));

								socketChannel = SocketChannel.open();
								socketChannel.connect(new InetSocketAddress(IP, 1991));
								sendData(socketChannel, "execution_time", true);
								String executionTime = receiveData(socketChannel);
								logAction("Test Data Classification using Models");
								result = classify(modelFileName, test, alg, executionTime, splitRatio);
								text = "Finished";
							} else {
								text = "Model is not trained by server";
							}
						} else {
							text = "Server no respond";
						}
					} catch (Exception ex) {
						Log.i("chz", null, ex);
					} finally {
						try {
							socketChannel.close();
						} catch(Exception ex) {}
					}

			return result;
		}

		protected void onPostExecute(String result) {
			Log.d("result",result);
			try {
//				Thread.sleep(1000);
			} catch (Exception e){}
			Toast.makeText(getApplicationContext(), text,Toast.LENGTH_SHORT).show();
			resultTv.setText(result);
			logToFile(result);
			logAction("Execution");
		}

		private void logToFile(String result){
			File file = new File(PROJECT_PATH + LOGFILE_PATH);
			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				if(!file.exists()){
					file.createNewFile();
				}
				fw=new FileWriter(file.getAbsoluteFile(),true);  //true means append to end
				bw=new BufferedWriter(fw);
				bw.write(result);
				bw.close();
				fw.close();
			} catch (Exception e){
				Toast.makeText(getApplicationContext(), "Error while saving log file",Toast.LENGTH_SHORT).show();
			}
		}

		private void logAction(String action){
			File file = new File(PROJECT_PATH + LOGFILE_PATH);
			FileWriter fw = null;
			BufferedWriter bw = null;
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			String sd = sdf.format(new Date(date.getTime()));
			String content = "Action " + action + " complete on: " + sd + "\n";
			if (action.equals("Execution")){
				content += "\n\n";
			}
			try {
				if(!file.exists()){
					file.createNewFile();
				}
				fw=new FileWriter(file.getAbsoluteFile(),true);  //true means append to end
				bw=new BufferedWriter(fw);
				bw.write(content);
				bw.close();
				fw.close();
			} catch (Exception e){
				Toast.makeText(getApplicationContext(), "Error while logging action",Toast.LENGTH_SHORT).show();
			}
		}

		private String classify(String modelFileName, Instances test, String alg, String executionTime, double splitRatio){
			try {
				String[] choices = alg.split("\\|");
				String[] trainingTime = executionTime.split("\\|");
				Classifier[] cls;
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PROJECT_PATH + modelFileName));
				cls = (Classifier[]) ois.readObject();
				ois.close();
				Thread[] threads = new Thread[choices.length];
				// classify test data using models
				for (int i = 0; i < choices.length;i++){
//					long startTime=System.currentTimeMillis();
//					Evaluation testEval = new Evaluation(test);
					ClassifyRun p = new ClassifyRun(choices[i], cls[i], test, splitRatio, trainingTime[i]);
					threads[i] =new Thread(p);
					threads[i].start();

//					testEval.evaluateModel(cls[i], test);
//					long endTime=System.currentTimeMillis();
//					if (choices[i].equals("nb")){
//						sb.append("\nNaive Bayes:").append("\n");
//					} else if (choices[i].equals("dt")){
//						sb.append("\nDecision Tree:").append("\n");
//					} else {
//						sb.append("\nLogistic Regression:").append("\n");
//					}
//					sb.append("Split ratio: ").append(splitRatio * 100).append("%\n");
//					sb.append("training time: ").append(trainingTime[i]).append("\n");
//					sb.append("classification time: ").append(endTime - startTime).append("ms\n");
//					sb.append(testEval.toMatrixString());
//					testEval.crossValidateModel(cls[i], test, 10, new Random(1));
//					sb.append(testEval.toClassDetailsString());
//					sb.append("\n");
				}
				for (Thread t : threads){
					t.join();
				}
				return mysb.toString();
			} catch (Exception e){}
			return "Error while classifying";
		}

		class ClassifyRun implements Runnable{
			String choice;
			Classifier cls;
			Instances test;
			double splitRatio;
			String trainingTime;

			ClassifyRun (String choice, Classifier cls, Instances test, double splitRatio, String trainingTime){
				this.choice = choice;
				this.cls = cls;
				this.test = test;
				this.splitRatio = splitRatio;
				this.trainingTime = trainingTime;
			}
			public void run() {
				StringBuffer sb = new StringBuffer();
				try {
					long startTime = System.currentTimeMillis();
					Evaluation testEval = new Evaluation(test);
					testEval.evaluateModel(cls, test);
					long endTime = System.currentTimeMillis();
					if (choice.equals("nb")){
						sb.append("\nNaive Bayes:").append("\n");
					} else if (choice.equals("dt")){
						sb.append("\nDecision Tree:").append("\n");
					} else {
						sb.append("\nLogistic Regression:").append("\n");
					}
					sb.append("Split ratio: ").append(splitRatio * 100).append("%\n");
					sb.append("training time: ").append(trainingTime).append("\n");
					sb.append("classification time: ").append(endTime - startTime).append("ms\n");
					sb.append(testEval.toMatrixString());
					testEval.crossValidateModel(cls, test, 10, new Random(1));
					sb.append(testEval.toClassDetailsString());
					sb.append("\n");
				} catch (Exception e){
					try {
						throw new Exception(e);
					} catch (Exception ee){

					}
				}
				lock.lock();
				mysb.append(sb);
				lock.unlock();
			}
		}

	}

}
