/*
Copyright (C) 2012 Haowen Ning, Xiaoyu Shi

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.dao.*;
import org.liberty.android.fantastischmemo.domain.*;
import org.liberty.android.fantastischmemo.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.mycommons.lang3.time.DateUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

public class DetailScreen extends AMActivity implements OnClickListener{
	
	private EditText idEntry;
	private EditText questionEntry;
	private EditText answerEntry;
	private EditText noteEntry;
	private EditText categoryEntry;
	private EditText lastLearnDateEntry;
	private EditText nextLearnDateEntry;
	private EditText gradeEntry;
	private EditText easinessEntry;
	private EditText acqRepsEntry;
	private EditText retRepsEntry;
	private EditText lapsesEntry;
	private EditText acqRepsSinceLapseEntry;
	private EditText retRepsSinceLapseEntry;
	private Button backButton;
	private Button updateButton;
	private Button resetButton;
	private String dbPath = "";

	private CardDao cardDao;
	private CategoryDao categoryDao;
	private LearningDataDao learningDataDao;
	private AnyMemoDBOpenHelper helper;	
    private Card currentCard;
    private InitTask initTask;
    private SaveCardTask saveCardTask;
	private int cardId = -1;

	public static String EXTRA_DBPATH = "dbpath";
	public static String EXTRA_CARD_ID = "card_id";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_screen);	

        initTask = new InitTask();
        initTask.execute((Void) null);
    }
    
    private void loadEntries() {

 	   	idEntry.setText("" + currentCard.getId());
		questionEntry.setText(currentCard.getQuestion());
		answerEntry.setText(currentCard.getAnswer());
		noteEntry.setText(currentCard.getNote());
	   	categoryEntry.setText(currentCard.getCategory().getName());
        Format formatter = new SimpleDateFormat("yyyy/MM/dd");
		lastLearnDateEntry.setText(formatter.format(currentCard.getLearningData().getLastLearnDate()));
		nextLearnDateEntry.setText(formatter.format(currentCard.getLearningData().getNextLearnDate()));
		gradeEntry.setText("" + currentCard.getLearningData().getGrade());
		easinessEntry.setText("" + currentCard.getLearningData().getEasiness());

		acqRepsEntry.setText("" + currentCard.getLearningData().getAcqReps());
		retRepsEntry.setText("" + currentCard.getLearningData().getRetReps());
		lapsesEntry.setText("" + currentCard.getLearningData().getLapses());
		acqRepsSinceLapseEntry.setText("" + currentCard.getLearningData().getAcqRepsSinceLapse());
		retRepsSinceLapseEntry.setText("" + currentCard.getLearningData().getRetRepsSinceLapse());
    }
    
    private String refreshEntries() {
        try {
            currentCard.setId(Integer.parseInt(idEntry.getText().toString()));
            currentCard.setQuestion(questionEntry.getText().toString());
            currentCard.setAnswer(answerEntry.getText().toString());
            currentCard.setNote(noteEntry.getText().toString());
            
            String[] parsers = {"yyyy/MM/dd"};
            currentCard.getLearningData().setLastLearnDate(DateUtils.parseDateStrictly(lastLearnDateEntry.getText().toString(), parsers));
            currentCard.getLearningData().setNextLearnDate(DateUtils.parseDateStrictly(nextLearnDateEntry.getText().toString(), parsers));

            currentCard.getLearningData().setGrade(Integer.parseInt(gradeEntry.getText().toString()));  
            currentCard.getLearningData().setEasiness(Float.parseFloat(easinessEntry.getText().toString()));
            currentCard.getLearningData().setAcqReps(Integer.parseInt(acqRepsEntry.getText().toString()));
            currentCard.getLearningData().setRetReps(Integer.parseInt(retRepsEntry.getText().toString()));
            currentCard.getLearningData().setLapses(Integer.parseInt(lapsesEntry.getText().toString()));
            currentCard.getLearningData().setRetRepsSinceLapse(Integer.parseInt(retRepsEntry.getText().toString()));
            currentCard.getLearningData().setAcqRepsSinceLapse(Integer.parseInt(acqRepsEntry.getText().toString()));
        } catch (ParseException e) {
            Log.e(TAG, "Input date format is not valid!");
            return e.toString();
        } catch (Exception e) {
            Log.e(TAG, "Input is not valid!");
            throw new RuntimeException(e);
        }
        return null;
    }

    private String saveEntries(){
        String error;
        try {
            error = refreshEntries();
            cardDao.update(currentCard);
            learningDataDao.update(currentCard.getLearningData());
        } catch (SQLException e) {
            Log.e(TAG, "Error saving data!");
            throw new RuntimeException(e);
        }

        return error;
    }
    
    public void onDestroy(){
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
    	super.onDestroy();
    	Intent resultIntent = new Intent();
    	setResult(Activity.RESULT_CANCELED, resultIntent);
    }
    
    public void onClick(View v){
    	if(v == backButton){
    		Intent resultIntent = new Intent();
    		setResult(Activity.RESULT_CANCELED, resultIntent);
    		finish();
    	}
    	if(v == resetButton){
            Calendar defaultLastLearnDate = Calendar.getInstance();
            defaultLastLearnDate.set(2010, 0, 1);

            Format formatter = new SimpleDateFormat("yyyy/MM/dd");
            lastLearnDateEntry.setText(formatter.format(defaultLastLearnDate.getTime()));
            Calendar defaultNextLearnDate = Calendar.getInstance();
            defaultNextLearnDate.set(2013, 0, 1);
            nextLearnDateEntry.setText(formatter.format(defaultNextLearnDate.getTime()));
            gradeEntry.setText("0");
            easinessEntry.setText("2.5");
            acqRepsEntry.setText("0");
            retRepsEntry.setText("0");
            lapsesEntry.setText("0");
            acqRepsSinceLapseEntry.setText("0");
            retRepsSinceLapseEntry.setText("0");
    	}
    	if(v == updateButton){
    		new AlertDialog.Builder(this)
                .setTitle(R.string.warning_text)
                .setMessage(R.string.item_update_warning)
                .setPositiveButton(R.string.ok_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
                            
                            saveCardTask = new SaveCardTask();
                            saveCardTask.execute((Void) null);

						}
					})
                .setNegativeButton(R.string.cancel_text, null)
                .show();
    	}
    }

    private class SaveCardTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        
        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(DetailScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(Void... params) {
            return saveEntries();
        }   

        @Override
        public void onPostExecute(String error) {
            progressDialog.dismiss();
            
            if (error != null) {
                new AlertDialog.Builder(DetailScreen.this)
                    .setTitle(R.string.warning_text)
                    .setMessage(R.string.exception_message)
                    .setPositiveButton(R.string.ok_text,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                            })
                .setNegativeButton(R.string.cancel_text, null)
                    .show();
            } else {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
    	    Bundle extras = getIntent().getExtras();
            
    	    if (extras != null) {
        	    dbPath = extras.getString(EXTRA_DBPATH);
                assert dbPath != null : "dbPath should not be null!";

    		    cardId = extras.getInt(EXTRA_CARD_ID, -1);
                Log.i(TAG, "cardId: " + cardId);
                assert cardId != -1 : "Card Id shouldn't be -1";
            }

            progressDialog = new ProgressDialog(DetailScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Void doInBackground(Void... params) {
        	try {
        		helper = AnyMemoDBOpenHelperManager.getHelper(DetailScreen.this, dbPath);
        		cardDao = helper.getCardDao();
        		categoryDao = helper.getCategoryDao();
        		learningDataDao = helper.getLearningDataDao();
        		
            	currentCard = cardDao.queryForId(cardId);
                categoryDao.refresh(currentCard.getCategory());
                learningDataDao.refresh(currentCard.getLearningData());
    		
        	} catch (SQLException e) {
        		Log.e(TAG, "Error creating daos!", e);
        		throw new RuntimeException("Dao creation error!");
        	}
            
            return null;
        }
        
        @Override
        public void onPostExecute(Void result) {
            idEntry = (EditText)findViewById(R.id.entry__id);
            questionEntry = (EditText)findViewById(R.id.entry_question);
            answerEntry = (EditText)findViewById(R.id.entry_answer);
            noteEntry = (EditText)findViewById(R.id.entry_note);
            categoryEntry = (EditText)findViewById(R.id.entry_category);
            lastLearnDateEntry = (EditText)findViewById(R.id.entry_last_learn_date);
            nextLearnDateEntry = (EditText)findViewById(R.id.entry_next_learn_date);
            gradeEntry = (EditText)findViewById(R.id.entry_grade);
            easinessEntry = (EditText)findViewById(R.id.entry_easiness);
            acqRepsEntry = (EditText)findViewById(R.id.entry_acq_reps);
            retRepsEntry = (EditText)findViewById(R.id.entry_ret_reps);
            lapsesEntry = (EditText)findViewById(R.id.entry_lapses);
            acqRepsSinceLapseEntry = (EditText)findViewById(R.id.entry_acq_reps_since_lapse);
            retRepsSinceLapseEntry = (EditText)findViewById(R.id.entry_ret_reps_since_lapse);

            backButton = (Button)findViewById(R.id.but_detail_back);
            updateButton = (Button)findViewById(R.id.but_detail_update);
            resetButton = (Button)findViewById(R.id.but_detail_reset);
            backButton.setOnClickListener(DetailScreen.this);
            resetButton.setOnClickListener(DetailScreen.this);
            updateButton.setOnClickListener(DetailScreen.this);

            questionEntry.setText(currentCard.getQuestion());

            loadEntries();

            progressDialog.dismiss();
        }
    }

}
