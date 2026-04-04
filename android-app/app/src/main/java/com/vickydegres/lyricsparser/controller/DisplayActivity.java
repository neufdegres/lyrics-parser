package com.vickydegres.lyricsparser.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vickydegres.lyricsparser.BuildConfig;
import com.vickydegres.lyricsparser.R;
import com.vickydegres.lyricsparser.controller.adapters.DisplayAdapter;
import com.vickydegres.lyricsparser.database.AppDatabase;
import com.vickydegres.lyricsparser.database.AppDatabaseSingleton;
import com.vickydegres.lyricsparser.database.Original;
import com.vickydegres.lyricsparser.database.SongInfo;
import com.vickydegres.lyricsparser.database.repositories.OriginalRepository;
import com.vickydegres.lyricsparser.database.repositories.SongRepository;
import com.vickydegres.lyricsparser.models.DisplayModel;
import com.vickydegres.lyricsparser.net.RequestQueueSingleton;
import com.vickydegres.lyricsparser.util.Language;
import com.vickydegres.lyricsparser.util.Lyrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DisplayActivity extends AppCompatActivity
                             implements DisplayActionDialogFragment.DisplayActionDialogListener{
    private AppDatabase mDatabase;
    private SongRepository mSongRep;
    private OriginalRepository mOriRep;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    DisplayModel mModel;
    TextView mTitle, mArtist, mLangText;
    ImageView mLangFlag;
    Button mRomanize, mTranslate;
    FloatingActionButton mAction;
    RecyclerView mRecyclerView;
    DisplayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mModel = new DisplayModel(getIntent().getIntExtra("selected", -1));

        if (getIntent().hasExtra("term")) {
            String term = getIntent().getStringExtra("term");
            mModel.setTerm(term);
        }

        // build (si nécessaire) de la database
        mDatabase = AppDatabaseSingleton.getInstance(getApplicationContext());
        mSongRep = new SongRepository(mDatabase.songDao());
        mOriRep = new OriginalRepository(mDatabase.lyricsDao());

        mTitle = findViewById(R.id.display_title);
        mArtist = findViewById(R.id.display_artist);
        mLangText = findViewById(R.id.display_lang_text);

        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mModel.getTitleMode() == DisplayModel.TITLE_MODE.ORIGINAL) {
                    mTitle.setText(mModel.getTitleRomanized());
                    mModel.setTitleMode(DisplayModel.TITLE_MODE.ROMANIZED);
                } else {
                    mTitle.setText(mModel.getTitle());
                    mModel.setTitleMode(DisplayModel.TITLE_MODE.ORIGINAL);

                }
            }
        });

        mArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected = mModel.getArtist();
                Intent artistActivityIntent = new Intent(DisplayActivity.this, ArtistActivity.class);
                artistActivityIntent.putExtra("selected", selected);
                startActivity(artistActivityIntent);
            }
        });

        mLangFlag = findViewById(R.id.display_lang_flag);

        mRomanize = findViewById(R.id.display_romanize_button);
        mTranslate = findViewById(R.id.display_translate_button);
        mAction = findViewById(R.id.display_action_button);

        mRomanize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRomanize.setEnabled(false);
                mRomanize.setText(R.string.display_romanization_ongoing);
                loadRomanization();
            }
        });

        mTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTranslate.setEnabled(false);
                mTranslate.setText(R.string.display_translation_ongoing);
                loadTranslation();
            }
        });

        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionDialog();
            }
        });

        mRecyclerView = findViewById(R.id.display_lyrics_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new DisplayAdapter(this, mModel);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
    }

    private void setHeader() {
        mTitle.setText(mModel.getTitle());
        mArtist.setText(mModel.getArtist());
        String lang = mModel.getLang().getName();
        lang = lang.substring(0,1).toUpperCase() + lang.substring(1).toLowerCase();
        mLangText.setText(lang);
        mLangFlag.setImageResource(mModel.getLang().getFlag());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        mCompositeDisposable.addAll(
                mSongRep.getSongById(mModel.getId())
                        .subscribe(songs -> {
                            SongInfo curr = songs.get(0);
                            mModel.setTitle(curr.getTitle());
                            mModel.setArtist(curr.getArtist());
                            String lang = curr.getLang();
                            mModel.setLang(new Language(lang));
                            if (!lang.equals("JP")) {
                                mRomanize.setEnabled(false);
                                mTranslate.setEnabled(false);
                            } else {
                                loadTitleRomanization();
                            }
                            setHeader();
                        }),
                mOriRep.getLyricsBySongId(mModel.getId())
                        .subscribe(texts -> {
                            Original ori = texts.get(0);
                            mModel.setOriginal(Lyrics.stringToLyrics(ori.getText()));
                            mAdapter.notifyDataSetChanged();
                        })
        );
    }

    private void loadTitleRomanization() {
        HashMap<String, Object> tmp = new HashMap<>();
        tmp.put("lines", List.of(mModel.getTitle()));

        sendRequestToAPI("rom_title", tmp);
    }

    private void loadRomanization() {
        HashMap<String, Object> tmp = new HashMap<>();
        tmp.put("lines", mModel.getOriginal().getLines());

        sendRequestToAPI("rom", tmp);
    }

    private void loadTranslation() {
        HashMap<String, Object> tmp = new HashMap<>();
        tmp.put("lines", mModel.getOriginal().getLines());

        sendRequestToAPI("tra", tmp);
    }

    private void sendRequestToAPI(String type, HashMap<String, Object> body) {
        // Instantiate the RequestQueue.
        RequestQueue queue =
                RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        String serverAddress = BuildConfig.SERVER_ADDRESS;
        String url = serverAddress + (type.equals("tra") ? "/translation" : "/romanization");

        // Request a string response from the provided URL.

        // Create JSON request
        JSONObject json = new JSONObject(body);
        Log.v("json", json.toString());

        JsonObjectRequest request = new JsonObjectRequest
            (Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONArray arr = response.optJSONArray("lines");

                    if (type.equals("rom_title")) {
                        String result = "[romanization impossible]";

                        if (arr != null && arr.length() > 0) {
                            result = arr.optString(0, result);
                        }

                        mModel.setTitleRomanized(result);
                    } else {
                        if (arr != null && arr.length() > 0) {
                            LinkedList<String> lines = new LinkedList<>();

                            for (int i = 0; i < arr.length(); i++) {
                                lines.add(arr.optString(i));
                            }

                            Lyrics ly = new Lyrics(lines);

                            if (type.equals("rom")) {
                                mModel.setRomanization(ly);
                                mRomanize.setText(R.string.display_romanized);
                            } else {
                                mModel.setTranslation(ly);
                                mTranslate.setText(R.string.display_translated);
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    String text = "";

                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        if (code >= 400) {
                            if (error.networkResponse.data != null) {
                                try {
                                    String json = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject obj = new JSONObject(json);

                                    String detail = obj.getString("detail");

                                    text = code + " : " + detail;
                                } catch (Exception e) {
                                    text = "Erreur HTTP " + code;
                                }
                            }
                            text = "Erreur HTTP " + code;
                        }
                    } else {
                        // text = error.toString();
                        text = "Erreur";
                    }

                    Toast.makeText(DisplayActivity.this, text, Toast.LENGTH_LONG).show();

                    switch(type) {
                        case "rom_title" :
                            mModel.setTitleRomanized("[romanization impossible]");
                            break;
                        case "rom" :
                            mRomanize.setText(R.string.display_romanize_error);
                            break;
                        case "tra" :
                            mTranslate.setText(R.string.display_translate_error);
                            break;
                    }
                }
            });

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void showActionDialog() {
        DisplayActionDialogFragment dialog = new DisplayActionDialogFragment();
        dialog.show(getSupportFragmentManager(), "DisplayActionDialogFragment");
    }


    public void onCopyClick(DialogFragment dialog) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("lyrics", mModel.getOriginal().toString());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onEditClick(DialogFragment dialog) {
        // TODO : créer page édit
        Intent editActivityIntent = new Intent(DisplayActivity.this, EditLyricsActivity.class);
        editActivityIntent.putExtra("toEdit", mModel.getId());
        startActivity(editActivityIntent);
    }

    @Override
    public void onDeleteClick(DialogFragment dialog) {
        // TODO : créer fragment "vous vous VRAIMENT supprimer ces lyrics ??"
    }
}