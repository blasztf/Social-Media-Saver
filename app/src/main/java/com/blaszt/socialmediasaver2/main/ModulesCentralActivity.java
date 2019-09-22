package com.blaszt.socialmediasaver2.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.helper.data.VolleyRequest;
import com.blaszt.socialmediasaver2.module.Module;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModulesCentralActivity extends AppCompatActivity {
    private RecyclerView content;
    private TextView noContent;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules_central);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupView();
        loadModulesFromCentral();
    }

    @Override
    public void onResume() {
        super.onResume();
        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        content.setLayoutManager(manager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupView() {
        content = findViewById(R.id.content);
        noContent = findViewById(R.id.no_content);
        title = findViewById(R.id.title);
    }

    private void loadModulesFromCentral() {
        final View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, "Fetching available modules on Modules Central...", Snackbar.LENGTH_LONG).show();
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                "http://sms.blaszt.gq",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<ModuleItem> modules = ModuleItem.parse(response);

                        if (!modules.isEmpty()) {
                            ModuleListAdapter adapter = new ModuleListAdapter(modules);
                            content.setAdapter(adapter);
                        } else {
                            noContent.setVisibility(View.VISIBLE);
                            content.setVisibility(View.INVISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(rootView, "Failed to fetch available modules on Modules Central!", Snackbar.LENGTH_LONG).show();
                        noContent.setText(error.getMessage());

                        noContent.setVisibility(View.VISIBLE);
                        content.setVisibility(View.INVISIBLE);
                    }
                }
        );
        VolleyRequest.with(this).addToQueue(request);
    }

    static class ModuleItem {
        private String source, name, logo;

        private static List<ModuleItem> parse(String jsonResponse) {
            int statusCode;
            JsonArray jsonModules;
            JsonObject jsonModule;
            ModuleItem module;

            List<ModuleItem> modules = new ArrayList<>();

            JsonObject json = new JsonParser().parse(jsonResponse).getAsJsonObject();
            statusCode = json.get("statusCode").getAsInt();
            if (statusCode == 0) {
                jsonModules = json.get("response").getAsJsonObject().getAsJsonArray("modules");
                for (JsonElement jsonElement : jsonModules) {
                    jsonModule = jsonElement.getAsJsonObject();
                    module = new ModuleItem(jsonModule.get("name").getAsString(), jsonModule.get("logo").getAsString(), jsonModule.get("source").getAsString());
                    modules.add(module);
                }
            }

            return modules;
        }

        private ModuleItem(String moduleName, String moduleLogo, String moduleSource) {
            name = moduleName;
            logo = moduleLogo;
            source = moduleSource;
        }
    }

    private class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ViewHolder> {
        private List<ModuleItem> modules;

        ModuleListAdapter(List<ModuleItem> modules) {
            this.modules = modules;
        }

        @NonNull
        @Override
        public ModuleListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ModuleListAdapter.ViewHolder(LayoutInflater.from(ModulesCentralActivity.this).inflate(R.layout.item_menu, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ModuleListAdapter.ViewHolder viewHolder, int i) {
            final ModuleItem module = modules.get(i);

            Glide.with(ModulesCentralActivity.this).load(module.logo).into(viewHolder.logo);
            viewHolder.name.setText(module.name);
            viewHolder.onClick(module);
        }

        @Override
        public int getItemCount() {
            return modules.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView logo;
            public TextView name;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                logo = itemView.findViewById(R.id.logo);
                name = itemView.findViewById(R.id.name);
            }

            void onClick(final ModuleItem module) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(ModulesCentralActivity.this)
                                .setMessage("Do you want to install this module?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        installModule(module);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                });
            }

            private void installModule(ModuleItem module) {
                ModuleInstaller.install(ModulesCentralActivity.this, module, new ModuleInstaller.OnInstalledListener() {
                    @Override
                    public void onInstalled() {
                        Snackbar.make(itemView.getRootView(), "Module has been successfully installed", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String msg) {
                        Snackbar.make(itemView.getRootView(), "Module was failed to install\nReason: " + msg, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private static class ModuleInstaller extends AsyncTask<Void, Integer, Boolean> {
        private File mInstallPath;
        private ModuleItem mModule;
        private String mErrorMessage;
        private OnInstalledListener mListener;

        public static ModuleInstaller install(Context context, ModuleItem module, OnInstalledListener listener) {
            ModuleInstaller installer = new ModuleInstaller(Module.getModulesBaseDir(context), module);
            installer.setListener(listener);
            installer.execute();
            return installer;
        }

        public interface OnInstalledListener {
            void onInstalled();
            void onError(String msg);
        }

        private ModuleInstaller(File installPath, ModuleItem module) {
            mInstallPath = installPath;
            mModule = module;
        }

        public void setListener(OnInstalledListener listener) {
            mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return installModule();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                mListener.onInstalled();
            }
            else {
                mListener.onError(mErrorMessage);
            }
        }

        private boolean installModule() {
            try {
                File copy = copy(new File(mModule.source), mInstallPath);
                return true;
            } catch (IOException e) {
                mErrorMessage = e.getMessage();
                return false;
            }
        }

        private void makeData(File moduleFile) {
//            new Module.ModuleData();
        }

        private File copy(File from, File to) throws IOException {
            to = new File(to, from.getName());

            FileInputStream reader = new FileInputStream(from);
            FileOutputStream writer = new FileOutputStream(to);

            byte[] buffer = new byte[1024];
            int i;
            while ((i = reader.read(buffer, 0, buffer.length)) != -1) {
                writer.write(buffer, 0, i);
            }

            writer.flush();
            reader.close();
            writer.close();

            return to;
        }
    }

//    private static class ModuleInstaller extends AsyncTask<String, Integer, String> {
//
//        interface OnInstalledListener {
//            void onInstalled();
//            void onError(String msg);
//        }
//
//        private File moduleInstallLocation;
//        private OnInstalledListener listener;
//
//        public static ModuleInstaller install(Context context, OnInstalledListener listener, Module module) {
//            ModuleInstaller instance = new ModuleInstaller(context, listener);
//            instance.execute(module.source);
//            return instance;
//        }
//
//        private ModuleInstaller(Context context, OnInstalledListener listener) {
//            moduleInstallLocation = com.blaszt.socialmediasaver2.module.Module.getModulesBaseDir(context);
//            this.listener = listener;
//        }
//
//        private String getFileName(HttpURLConnection connection) {
//            String filename;
//            String url = connection.getURL().toString();
//
//            // Try with header "Content-Disposition" to find the file name.
//            String disposition = connection.getHeaderField("Content-Disposition");
//            int index;
//            if (disposition != null && (index = disposition.indexOf("filename=")) >= 0) {
//                filename = disposition.substring(index + 10);
//            } else {
//                // Try with parsing url link to find the file name.
//                int beginIndex = url.lastIndexOf("/");
//                int endIndex = url.lastIndexOf(".");
//
//                filename = url.substring(beginIndex + 1, endIndex);
//                // Just use the rest of url after the last dot as extension (hopefully there is no parameter behind it).
//                filename += url.substring(endIndex);
//            }
//
//            return filename;
//        }
//
//        private int determineBufferSize(long lengthFile) {
//            final int LIMIT_BUFFER_SIZE = 8 * 1024;
//            int bufferSize = 0;
//            if (lengthFile >= LIMIT_BUFFER_SIZE) {
//                bufferSize = LIMIT_BUFFER_SIZE;
//            } else if (lengthFile >= 1024) {
//                for (int i = 1; i * 1024 < lengthFile; i *= 2) {
//                    bufferSize = i * 1024;
//                }
//            } else {
//                bufferSize = 1024;
//            }
//            return bufferSize;
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            HttpURLConnection connection;
//            InputStream stream;
//            File file;
//            FileOutputStream output;
//            try {
//                connection = (HttpURLConnection) new URL(strings[0]).openConnection();
//                stream = connection.getInputStream();
//                int lengthFile = connection.getContentLength();
//
//                file = new File(moduleInstallLocation, getFileName(connection));
//
//                output = new FileOutputStream(file);
//                byte[] data = new byte[determineBufferSize(lengthFile)];
//                int current, total = 0;
//                while ((current = stream.read(data, 0, data.length)) != -1) {
//                    output.write(data, 0, current);
//                    total += current;
//                }
//                return null;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String errorMsg) {
//            if (listener != null) {
//                if (errorMsg == null) listener.onInstalled();
//                else listener.onError(errorMsg);
//            }
//        }
//    }

}
