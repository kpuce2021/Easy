package com.example.easydashcam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DownloadAdapter downloadAdapter= new DownloadAdapter(new ArrayList<TableResponse>());
    private DownloadViewModel downloadViewModel;
    private Context downloadContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        downloadContext=this;

        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(downloadAdapter);
        downloadViewModel= new ViewModelProvider(this).get(DownloadViewModel.class);

        downloadViewModel.mutableLiveData.observe(this, new Observer<ArrayList<TableResponse>>() {
            @Override
            public void onChanged(ArrayList<TableResponse> tableResponses) {
                updateUI(tableResponses);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadViewModel.accessDB();
    }

    public void updateUI(ArrayList<TableResponse> arr){
        downloadAdapter=new DownloadAdapter(arr);
        recyclerView.setAdapter(downloadAdapter);
    }


    class DownloadHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        public DownloadHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.text_download_item);
            textView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(downloadContext, DownloadService.class);
                    intent.putExtra("fileName", textView.getText());
                    startService(intent);

                }
            });
        }

        public void bind(TableResponse tableResponse){
            textView.setText(tableResponse.getTitle());
        }

    }

    class DownloadAdapter extends RecyclerView.Adapter<DownloadHolder>{
        private ArrayList<TableResponse> arr;

        public DownloadAdapter(ArrayList<TableResponse> dbResponse) {
            arr=dbResponse;
        }

        @NonNull
        @Override
        public DownloadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=getLayoutInflater().inflate(R.layout.download_item, parent, false);
            return new DownloadHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DownloadHolder holder, int position) {
            holder.bind(arr.get(position));
        }

        @Override
        public int getItemCount() {
            return arr.size();
        }

    }
}