package ca.jasonsim.rssapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static ca.jasonsim.rssapp.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private SAXParser saxParser;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<String> title = new ArrayList<String>();

    private ArrayList<String> description = new ArrayList<String>();

    private ArrayList<String> links = new ArrayList<String>();

    Context context = MainActivity.this;




    //an AsyncTask (separate thread) for network access (RSS Processing)
    class RssProcessingTask extends AsyncTask<Void, Void, Void> {

        //does not have access to the UI thread
        //the "main" method of the thread
        @Override
        protected Void doInBackground(Void... voids) {
            //set up a SAXParser for processing an RSS feed
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            try {
                saxParser = saxParserFactory.newSAXParser();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            //create an HttpURL connection
            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL("http://www.jasonsim.ca/rss/rss-sports-nhl.xml");
                connection = (HttpURLConnection)url.openConnection();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //create an instance of our rssHandler
            RssHandler rssHandler = new RssHandler();

            //let the parsing begin
            try {
                saxParser.parse(connection.getInputStream(), rssHandler);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //has access to the UI
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("Jay", "onPostExecute()");
            title.remove(0);
            title.remove("CBC.ca");

            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, title);

            final ListView listView = (ListView) findViewById(R.id.listview_rss);
            listView.setAdapter(itemsAdapter);

            itemsAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Intent descriptionActivity = new Intent(MainActivity.this, DescriptionActivity.class);

                    String link = links.get(position + 2);
                    String itemDescription = description.get(position + 1);

                    Bundle extras = new Bundle();

                    extras.putString("key", link);
                    extras.putString("description", itemDescription);
                    descriptionActivity.putExtras(extras);
                    startActivity(descriptionActivity);
                }
            });

        }
    }

    //SAX handler
    class RssHandler extends DefaultHandler {

        //string builder for handing multiple calls to characters()
        //for a single element
        private StringBuilder stringBuilder;

        //boolean flags for keeping track of what elements we're in
        private boolean inTitle;
        private boolean inLink;
        private boolean inDescription;


        public RssHandler() {
            title = new ArrayList<String>();
            links = new ArrayList<String>();
            description = new ArrayList<String>();
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            Log.d("Jay", "startDocument()");
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            Log.d("Jay", "endDocument()");
            for(int i = 0; i < title.size(); i++) {
                Log.d("Jay", "TITLE: " + title.get(i));
                Log.d("Jay", "LINK: " + links.get(i));
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            Log.d("Jay", "startElement(): " + qName);

            switch (qName) {
                case "title":
                    inTitle = true;
                    //create a stringBuilder to hold characters
                    stringBuilder = new StringBuilder();
                    break;
                case "link":
                    inLink = true;
                    //create a stringBuilder to hold characters
                    stringBuilder = new StringBuilder();
                    break;
                case "description":
                    inDescription = true;
                    //create a stringBuilder to hold characters
                    stringBuilder = new StringBuilder();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            Log.d("Jay", "endElement(): " + qName);

            switch (qName) {
                case "title":
                    inTitle = false;
                    title.add(stringBuilder.toString());
                    break;
                case "link":
                    inLink = false;
                    links.add(stringBuilder.toString());
                    break;
                case "description":
                    inDescription = false;
                    description.add(stringBuilder.toString());
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            //what element(s) are we in?
            if(inTitle) {
                stringBuilder.append(ch, start, length);
                //title.add(s);
            }

            //what element(s) are we in?
            else if(inLink) {
                stringBuilder.append(ch, start, length);
                //title.add(s);
            }

            //what element(s) are we in?
            else if(inDescription) {
                stringBuilder.append(ch, start, length);
                //title.add(s);
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        Log.d("Jay", "onCreate");

        //create an instance of our AsyncTask
        RssProcessingTask rssProcessingTask = new RssProcessingTask();

        rssProcessingTask.execute();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        // Setup refresh listener which triggers new data loading
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssProcessingTask rssProcessingTask = new RssProcessingTask();
                rssProcessingTask.execute();

            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


}
