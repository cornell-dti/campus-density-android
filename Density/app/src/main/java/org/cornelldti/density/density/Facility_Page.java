package org.cornelldti.density.density;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Facility_Page.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Facility_Page#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Facility_Page extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM = "Facility_Object";

    private TextView facility_name;

    private ImageButton backButton;

    private ImageButton favoriteButton;

    private boolean favorite;

    private Facility facility;

    private OnFragmentInteractionListener mListener;

    public Facility_Page() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param facility Parameter 1.
     * @return A new instance of fragment Facility_Page.
     */
    // TODO: Rename and change types and number of parameters
    public static Facility_Page newInstance(Facility facility) {
        Facility_Page fragment = new Facility_Page();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, facility);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            facility = (Facility)getArguments().getSerializable(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_facility__page, container, false);

        facility_name = v.findViewById(R.id.f_name);
        backButton = v.findViewById(R.id.backButton);
        favoriteButton = v.findViewById(R.id.favoriteFacility);

        initializeView();
        return v;
    }

    private void initializeView()
    {
        facility_name.setText(facility.getName());
        favorite = facility.isFavorite();
        if(favorite)
        {
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.filled_heart));
        }
        else
        {
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.unfilled_heart));
        }
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(favorite)
                {
                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.unfilled_heart));
                    favorite = false;
                }
                else
                {
                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.filled_heart));
                    favorite = true;
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // MAKE REQUEST TO API TO UPLOAD NEW USER FAVORITES DATA TODO
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(Facility_Page.this).commit();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
