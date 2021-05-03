package com.hitasoft.app.external;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hitasoft.app.helper.PlaceAutocomplete;
import com.hitasoft.app.joysale.LocationActivity;
import com.hitasoft.app.joysale.R;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter {

    private static final String TAG = PlacesAutoCompleteAdapter.class.getSimpleName();
    private final PlaceAutoCompleteInterface mListener;
    public ArrayList<PlaceAutocomplete> resultLists = new ArrayList<>();
    private Context context;
    private String countryCode, from;
    // Create a new Places client instance.
    PlacesClient placesClient;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private int itemLayout;

    public PlacesAutoCompleteAdapter(Context context, int itemLayout, String countryCode, PlacesClient placesClient, String from) {
        super(context, itemLayout);
        this.context = context;
        this.countryCode = countryCode;
        this.placesClient = placesClient;
        this.itemLayout = itemLayout;
        this.mListener = (PlaceAutoCompleteInterface) context;
        this.from = from;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }
        TextView txtSuggestion = view.findViewById(R.id.txtSuggestion);
        String location = getItem(position).getPlaceName() + ", " + getItem(position).getPlaceFullAddress();
        txtSuggestion.setText(location);
        txtSuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onPlaceClick(resultLists.get(position), position, location);
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return resultLists.size();
    }

    @Override
    public PlaceAutocomplete getItem(int index) {
        return resultLists.get(index);
    }

    public void getAutoComplete(String input, AutocompleteSessionToken token) {
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(LocationActivity.BOUNDS);
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request;
        if (from.equals(Constants.TAG_PROFILE)) {
            request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(bounds)
                    .setCountry(countryCode)
                    .setTypeFilter(TypeFilter.REGIONS)
                    .setSessionToken(token)
                    .setQuery("" + input)
                    .build();
        } else {
            request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(bounds)
                    .setCountry(countryCode)
                    .setSessionToken(token)
                    .setQuery("" + input)
                    .build();
        }
        resultLists = new ArrayList<>();
        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                resultLists.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getPrimaryText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD), null));
            }
            notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getMessage());
            }
        });
    }

    public interface PlaceAutoCompleteInterface {
        void onPlaceClick(PlaceAutocomplete placeAutocomplete, int position, String location);
    }
}
