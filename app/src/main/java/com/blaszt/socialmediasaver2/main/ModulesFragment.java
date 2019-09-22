package com.blaszt.socialmediasaver2.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.module.Module;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ModulesFragment extends BaseFragment {
    private RecyclerView content;
    private TextView noContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modules, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupView();
    }

    @Override
    public void onResume() {
        super.onResume();
        GridLayoutManager manager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        content.setLayoutManager(manager);

        ArrayList<Module> modules = getModules();

        if (!modules.isEmpty()) {
            ModuleListAdapter adapter = new ModuleListAdapter(modules);
            content.setAdapter(adapter);
        } else {
            noContent.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
        }
    }

    private void setupView() {
        View view = getView();
        if (view != null) {
            content = view.findViewById(R.id.content);
            noContent = view.findViewById(R.id.no_content);
        }
    }

    private ArrayList<Module> getModules() {
        ArrayList<Module> modules = new ArrayList<>();
        File[] apks = Module.getAllModulesApk(getContext());
        Module module;
        for (File apk : apks) {
            module = new Module(getContext(), apk.getName());
            modules.add(module);
        }

        return modules;
    }

    private class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ViewHolder> {
        private List<Module> modules;

        ModuleListAdapter(List<Module> modules) {
            modules.add(0, null);
            this.modules = modules;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(ModulesFragment.this.getContext()).inflate(R.layout.item_menu, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final Module module = modules.get(i);

            if (module == null) {
                RequestOptions options = new RequestOptions()
                        .override(96);
                Glide.with(getContext()).asBitmap().load(R.drawable.ic_modules_central).apply(options).into(viewHolder.logo);

                viewHolder.name.setText("Modules Central");
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ModulesFragment.this.getContext(), ModulesCentralActivity.class);
                        ModulesFragment.this.startActivity(intent);
                    }
                });
                viewHolder.logo.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));
            } else {
                RequestOptions options = new RequestOptions()
                        .override(96);

                byte[] logo = Base64.decode(module.getLogo(), Base64.DEFAULT);

                Glide.with(getContext()).asBitmap().load(logo).apply(options).into(viewHolder.logo);
                viewHolder.name.setText(module.getName());
//                viewHolder.logo.setColorFilter(0xFFFFFFFF);
            }
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
        }
    }
}
