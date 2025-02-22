package com.smartpot.botanicaljournal.Views;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartpot.botanicaljournal.Controllers.PlantController;
import com.smartpot.botanicaljournal.Models.Plant;
import com.smartpot.botanicaljournal.Helpers.PlantViewState;
import com.smartpot.botanicaljournal.R;
import com.smartpot.botanicaljournal.Helpers.VolleyCallback;

import java.util.ArrayList;
import java.util.Date;

public class PlantFragment extends Fragment {

    protected static final String TAG = "Plant Fragment";

    private PlantController pc;

    private ArrayList<Plant> plants;
    private PlantAdapter plantAdapter;

    public static PlantFragment newInstance() {
        return new PlantFragment();
    }

    private SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Botanical Journal"); // Set title of navigation bar

        // Initialize Database Controller
        pc = new PlantController(getContext());

        // Load plants from the database
        plants = pc.getPlants();

        // Create plant adapter
        plantAdapter = new PlantAdapter(getContext(), plants);

        updatePlants();

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("TAG", "onRefresh called from SwipeRefreshLayout");

                        // Update plant data here
                        updatePlants();
                        refreshLayout.setRefreshing(false);
                    }
                }
        );

        //Get reference to ListView
        ListView plantView = view.findViewById(R.id.plantListView);
        LinearLayout noPlantsView =  view.findViewById(R.id.noPlantsView);

        //Set ListView layout
        plantView.setEmptyView(noPlantsView); // Set default layout when list is empty
        plantView.setAdapter(plantAdapter);

        //Set OnClickListener to ListView Item
        plantView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){
                NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(0).setChecked(false);
                Plant plant = (Plant)parent.getAdapter().getItem(position);
                ManagePlantFragment managePlantFragment = ManagePlantFragment.newInstance(PlantViewState.VIEWPLANT);
                managePlantFragment.setPlant(plant);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, managePlantFragment).addToBackStack(null).commit();
            }
        });

        // Replace main fragment with add plant fragment when button is clicked
        Button addPlantButton = view.findViewById(R.id.addPlantButton);
        addPlantButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                        navigationView.getMenu().getItem(1).setChecked(true);
                        ManagePlantFragment managePlantFragment = ManagePlantFragment.newInstance(PlantViewState.ADDPLANT);
                        managePlantFragment.setPlant(new Plant());
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, managePlantFragment).addToBackStack(null).commit();
                    }
                }
        );

        return view;
    }


    private void updatePlants() {

        for(int i = 0; i < plants.size(); i++) {
            //What Plant to update in Array
            final int plantIndex = i;

            //Pot ID
            String potId = plants.get(i).getPotId();

            //If potID is not null, make request to Server
            if (!potId.equals("")) {
                pc.updatePlant(potId, new VolleyCallback() {
                    @Override
                    public void onResponse(boolean success, String potId, int moistureLevel, int waterLevel, Date timeStamp) {
                        if (success) {
                            Plant p = plants.get(plantIndex);
                            p.setMoistureLevel(moistureLevel);
                            p.setLastWatered(timeStamp);
                            p.setWaterLevel(waterLevel);
                            pc.updatePlant(p);
                            plantAdapter.notifyDataSetChanged();
                        }
                        else {
                            Log.e(TAG, "Couldn't get Data for: " + potId);
                        }
                    }
                });
            }

        }
    }


}