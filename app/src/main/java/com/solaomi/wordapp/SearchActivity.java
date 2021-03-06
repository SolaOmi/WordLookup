package com.solaomi.wordapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import net.jeremybrooks.knicker.dto.WordOfTheDay;

public class SearchActivity extends AppCompatActivity {

//    private static final String LOG_TAG = SearchActivity.class.getName();

    private static final int WORD_OF_THE_DAY_LOADER_ID = 1;

    private SearchView mWordLookupSearchView;

    private LinearLayout mWordOfTheDayContainer;

    private TextView mWordOfTheDayWordTextView;

    private TextView mWordOfTheDayDefinitionTextView;

    private TextView mWordOfTheDayExampleTextView;

    private ImageView mWordnikApiAttributionImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Match layout views to their corresponding java variables
        mWordLookupSearchView = findViewById(R.id.word_search_view);
        mWordOfTheDayContainer = findViewById(R.id.word_of_the_day_container);

        mWordOfTheDayWordTextView = findViewById(R.id.word_of_the_day);
        mWordOfTheDayDefinitionTextView = findViewById(R.id.word_of_the_day_definition);
        mWordOfTheDayExampleTextView = findViewById(R.id.word_of_the_day_example);

        mWordnikApiAttributionImageView = findViewById(R.id.wordnik_api_attribution);

        Button wordOfTheDayButton = findViewById(R.id.word_of_the_day_button);

        // Add a click listener on Word-of-the-Day button to lookup more details.
        wordOfTheDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = mWordOfTheDayWordTextView.getText().toString();
                startWordActivity(word);
            }
        });

        // Set a query text listener on the SearchView to lookup a word.
        mWordLookupSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String word) {
                startWordActivity(word);
                return true;
            }

            // Links the Wordnik Logo Image to Wordnik API website per the
            // Attribution Requirement
            @Override
            public boolean onQueryTextChange(final String word) {
                mWordnikApiAttributionImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = getString(R.string.wordnik_base_url) + word;

                        Uri webpage = Uri.parse(url);

                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });

                return true;
            }
        });

        // Implement a loadercallback for the Word-of-the-Day loader.
        LoaderCallbacks<WordOfTheDay> wordOfTheDayLoaderListener =
                new LoaderCallbacks<WordOfTheDay>() {
                    @Override
                    public Loader<WordOfTheDay> onCreateLoader(int i, Bundle bundle) {
                        return new WordOfTheDayLoader(SearchActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<WordOfTheDay> loader, WordOfTheDay wordOfTheDay) {
                        updateUI(wordOfTheDay, true);
                    }

                    @Override
                    public void onLoaderReset(Loader<WordOfTheDay> loader) {
                    }
                };

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            // A reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in the wordOfTheDayLoaderListener for the LoaderCallbacks parameter.
            loaderManager.initLoader(WORD_OF_THE_DAY_LOADER_ID, null,
                    wordOfTheDayLoaderListener);
        } else {
            updateUI(null, false);
        }
    }

    /**
     * Update the screen to display the relevant information depending on the data returned from
     * the loader and the network state.
     *
     * @param wordOfTheDay is a WordOfTheDay object containing the Word-of-the-Day, including
     *                     definitions and example sentences.
     * @param isConnected  is a boolean that flags for internet connection.
     */
    private void updateUI(WordOfTheDay wordOfTheDay, boolean isConnected) {
        String error_message;

        if (isConnected && wordOfTheDay != null) {
            // Get the Text (!!! do not put strings in xml file, it will mess up the underline)
            String wordOfTheDayWord = wordOfTheDay.getWord();
            String wordOfTheDayDefinition =
                    "<u>Definition:</u> "
                    + wordOfTheDay.getDefinitions().get(0).getText();

            String wordOfTheDayExample =
                    "<u>Example:</u> "
                    + wordOfTheDay.getExamples().get(0).getText();

            // Set the text
            mWordOfTheDayWordTextView.setText(wordOfTheDayWord);
            mWordOfTheDayDefinitionTextView.setText(Html.fromHtml(wordOfTheDayDefinition));
            mWordOfTheDayExampleTextView.setText(Html.fromHtml(wordOfTheDayExample));
        } else if (isConnected) {
            // Show bad server response error message
            error_message = getString(R.string.bad_server_response);
            showErrorMessage(error_message);
        } else {
            // Show no internet connection error message
            error_message = getString(R.string.no_internet_connection);
            showErrorMessage(error_message);
        }
    }

    /**
     * Helper class to start the Word Activity.
     *
     * @param word is a String of the word to be looked up in the Word Activity.
     */
    private void startWordActivity(String word) {
        // Create a new intent to open the {@link WordActivity}
        Intent wordIntent = new Intent(SearchActivity.this, WordActivity.class);

        // Pass word to the new Activity.
        wordIntent.putExtra("word", word);

        // Start the new activity
        startActivity(wordIntent);
    }

    /**
     * This method will make the error message visible and hide the rest of the layout
     * View.
     *
     * @param message is the error message to be displayed.
     */
    private void showErrorMessage(String message) {
        // First, hide the currently visible data
        mWordLookupSearchView.setVisibility(View.GONE);
        mWordOfTheDayContainer.setVisibility(View.GONE);
        mWordnikApiAttributionImageView.setVisibility(View.GONE);

        // Then, show the error
        TextView emptyStateTextView = findViewById(R.id.empty_view);
        emptyStateTextView.setText(message);
    }

}
