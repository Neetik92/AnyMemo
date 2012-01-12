package org.liberty.android.fantastischmemo.ui;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardEditor;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class CategoryEditorFragment extends DialogFragment implements View.OnClickListener {

    private CardEditor mActivity;
    private CategoryDao categoryDao;
    private CardDao cardDao;
    private Card currentCard;
    private List<Category> categories;
    private static final String TAG = "CategoryEditorFragment";
    private CategoryAdapter categoryAdapter;
    private ListView categoryList;
    private Button okButton;
    private Button newButton;
    private Button deleteButton;
    private Button editButton;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (CardEditor)activity;
        categoryDao = mActivity.categoryDao;
        cardDao = mActivity.cardDao;
        currentCard = mActivity.currentCard;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_dialog, container, false);
        categoryList = (ListView)v.findViewById(R.id.category_list);
        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        InitTask initTask = new InitTask();
        initTask.execute((Void)null);
        newButton = (Button)v.findViewById(R.id.button_new);
        okButton = (Button)v.findViewById(R.id.button_ok);
        editButton = (Button)v.findViewById(R.id.button_edit);
        deleteButton = (Button)v.findViewById(R.id.button_delete);
        enableListeners();

        return v;
    }


    public void onClick(View v) {
        if (v == okButton) {
            SaveCardTask saveCardTask = new SaveCardTask();
            saveCardTask.execute((Void)null);
        }
        if (v == newButton) {
        }
        if (v == editButton) {
        }
        if (v == deleteButton) {
        }
    }

    /*
     * This task will mainly populate the categoryList
     */
    private class InitTask extends AsyncTask<Void, Void, Void> {

		@Override
        public void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
            categoryAdapter = new CategoryAdapter(mActivity, android.R.layout.simple_list_item_single_choice);
            assert categoryList != null : "Couldn't find categoryList view";
            assert categoryAdapter != null : "New adapter is null";
            categoryList.setAdapter(categoryAdapter);
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                categories = categoryDao.queryForAll();
            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.addAll(categories);
            mActivity.setProgressBarIndeterminateVisibility(false);
        }
    }


    /*
     * This task will save the card and exit the dialog
     */
    private class SaveCardTask extends AsyncTask<Void, Category, Void> {
        private Category selectedCategory;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
        }

        @Override
        public Void doInBackground(Void... params) {
            assert selectedCategory != null : "Null category is selected. This shouldn't happen";
            try {
                currentCard.setCategory(selectedCategory);
                cardDao.update(currentCard);
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category of current card", e);
                throw new RuntimeException("Error updating the category of current card");
            }
            return null;
        }

        @Override
        public void onCancelled(){
            CategoryEditorFragment.this.dismiss();
        }

        @Override
        public void onPostExecute(Void result){
            mActivity.setProgressBarIndeterminateVisibility(false);
            mActivity.updateViews();
            CategoryEditorFragment.this.dismiss();
        }
    }

    protected class CategoryAdapter extends ArrayAdapter<Category>{

        public CategoryAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        public void addAll(List<Category> lc) {
            for (Category c : lc) {
                add(c);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            CheckedTextView v = (CheckedTextView)convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /* Reuse the filebrowser's resources */
                v = (CheckedTextView)li.inflate(android.R.layout.simple_list_item_single_choice, null);
            }
            Category item = getItem(position);
            if (item.getName().equals("")) {
                v.setText(R.string.uncategorized_text);
            } else {
                v.setText(item.getName());
            }
            return v;
        }
    }

    private void enableListeners() {
        okButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
    }

    private void disableListeners() {
        okButton.setOnClickListener(null);
        deleteButton.setOnClickListener(null);
        newButton.setOnClickListener(null);
        editButton.setOnClickListener(null);
    }
}
