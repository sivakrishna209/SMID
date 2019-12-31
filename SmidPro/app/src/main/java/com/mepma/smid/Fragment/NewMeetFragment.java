package com.mepma.smid.Fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
//import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mepma.smid.Model.SMMember;
import com.mepma.smid.R;
import com.mepma.smid.Util.Const;
import com.mepma.smid.Util.DataHolder;
import com.mepma.smid.Util.Util;
import com.mepma.smid.http.AppHelper;
import com.mepma.smid.http.AsyncHttpClient;
import com.mepma.smid.http.RequestParams;
import com.mepma.smid.http.StringHttpResponseHandler;
import com.mepma.smid.http.VolleyMultipartRequest;
import com.mepma.smid.http.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import im.delight.android.location.SimpleLocation;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewMeetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewMeetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewMeetFragment extends Fragment implements LocationListener {

    private Unbinder unbinder;

    @BindView(R.id.sp_status_change) Spinner _spStatusChange;
    @BindView(R.id.btnTakeAttendance) Button _btnTakeAttendance;
    @BindView(R.id.etAttendedCount) EditText _etAttendedCount;
    @BindView(R.id.btn_take_photo) Button _btnTakePhoto;
    @BindView(R.id.captured_image) ImageView _capturedImage;
    @BindView(R.id.btn_submit_meet) Button _btnSubmitMeeting;
    @BindView(R.id.bg_attendance) LinearLayout _bgAttendance;
    @BindView(R.id.bg_capturePhoto) LinearLayout _bgCapurePhoto;
    @BindView(R.id.bg_meetDescription) LinearLayout _bgMeetDescription;
    @BindView(R.id.etMeetDiscussion) EditText _etMeetingDiscussion;

    @BindView(R.id.bg_newMeetingDate) LinearLayout _bgNewMeetingDate;
    @BindView(R.id.sp_meet_date_selection) Spinner _spChangeDate;
    @BindView(R.id.dateField) EditText _dateField;
    @BindView(R.id.bg_calenderField) LinearLayout _bgCalenderField;
    @BindView(R.id.btn_selectDate) ImageButton _btnSelectDate;
    @BindView(R.id.etLastMeetDescription) EditText _etLastMeetDescription;



    private List<SMMember> mMembers;
    private String mMeetingId ;
    private String mMeetingStatusString ;
    private ArrayList<SMMember> mAttendedMembers = new ArrayList<SMMember>();


    private OnFragmentInteractionListener mListener;
    private SimpleLocation mCurrentLocation;
    private String mImageFilePath;
    private Bitmap mBitMap;

    DatePickerDialog mDatePickerDialog;
    LocationManager locationManager;
    String mprovider;


    private Const.ChangeMeetingStatus mMeetingStatus = Const.ChangeMeetingStatus.valueOf(0);


    public NewMeetFragment() {
        // Required empty public constructor
    }

    public static NewMeetFragment newInstance(String meetingId, String meetingStatusString) {
        NewMeetFragment fragment = new NewMeetFragment();
        fragment.mMeetingId = meetingId;
        fragment.mMeetingStatusString = meetingStatusString;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_meet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this,getActivity());
        configureListeners();
        retrieveMembers();
        configureUI();

        configureLocation();

    }

    private void configureLocation() {

        Location currentLocation = DataHolder.getInstance().currentLocation;

        if (currentLocation != null) {
            Log.d("LocationManager","configureLocation: "+currentLocation.toString());
        } else {
            Log.d("LocationManager","configureLocation: is empty");

        }


       /* locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        mprovider = locationManager.getBestProvider(criteria, false);

        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(mprovider);
            locationManager.requestLocationUpdates(mprovider, 15000, 1, this);

            if (location != null) {
                //onLocationChanged(location);
                Log.d("Locts", String.format("Locations : %f %", location.getLatitude(), location.getLongitude()));

            } else
                Toast.makeText(getActivity().getBaseContext(), "No Location Provider Found Check Your Code", Toast.LENGTH_SHORT).show();
        } */

    }



    private void configureUI() {

        String[] items = new String[] { "Yes", "Cancelled", "Not yet" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        _spStatusChange.setAdapter(adapter);
        _spStatusChange.setSelection(mMeetingStatus.getValue());
        _spStatusChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMeetingStatus =  Const.ChangeMeetingStatus.valueOf(position);
                onStatusChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        onStatusChanged();



        String[] dItems = new String[] { "Yes", "No" };
        ArrayAdapter<String> dAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dItems);
        _spChangeDate.setAdapter(dAdapter);
        _spChangeDate.setSelection(0);
        _spChangeDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onNewDateSelectionChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        onNewDateSelectionChanged(0);

        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(calendar);
            }

        };
        mDatePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));



        _btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });

        _dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });





    }

    private void updateDateLabel(Calendar calendar) {
        String format = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        _dateField.setText(sdf.format(calendar.getTime()));

    }

    private void onStatusChanged() {

        if (mMeetingStatus == Const.ChangeMeetingStatus.yes) {
            _bgAttendance.setVisibility(View.VISIBLE);
            _bgCapurePhoto.setVisibility(View.VISIBLE);
            _bgNewMeetingDate.setVisibility(View.GONE);
        } else if (mMeetingStatus == Const.ChangeMeetingStatus.cancelled) {
            _bgAttendance.setVisibility(View.GONE);
            _bgCapurePhoto.setVisibility(View.GONE);
            _bgNewMeetingDate.setVisibility(View.GONE);
        } else if (mMeetingStatus == Const.ChangeMeetingStatus.notyet) {
            _bgAttendance.setVisibility(View.GONE);
            _bgCapurePhoto.setVisibility(View.GONE);
            _bgNewMeetingDate.setVisibility(View.VISIBLE);
        }
    }

    private void onNewDateSelectionChanged(int position) {
        if (position == 0) {
            _bgCalenderField.setVisibility(View.VISIBLE);
        } else if (position == 1) {
            _bgCalenderField.setVisibility(View.GONE);
        }

    }


    private void configureListeners() {

        _btnTakeAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle("Take Attendance");
                String[] listItems = SMMember.extractNames(mMembers);
                boolean[] checkedItems = new boolean[listItems.length];

                final List<SMMember> selectedList = new ArrayList<>();
                selectedList.addAll(mAttendedMembers);

                Iterator<SMMember> itr = mMembers.iterator();
                int i= 0;
                while (itr.hasNext()) {
                    checkedItems[i] = selectedList.contains(itr.next()) ? true : false;
                    i++;
                }

                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            if (!selectedList.contains(mMembers.get(position))) {
                                selectedList.add(mMembers.get(position));
                            }
                        } else if (selectedList.contains(mMembers.get(position))) {
                            selectedList.remove(mMembers.get(position));
                        }

                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        mAttendedMembers.clear();
                        mAttendedMembers.addAll(selectedList);

                        _etAttendedCount.setText(String.format("%d",mAttendedMembers.size()));
                    }
                });

                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

//                mBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        for (int i = 0; i < checkedItems.length; i++) {
//                            checkedItems[i] = false;
//                            mUserItems.clear();
//                            mItemSelected.setText("");
//                        }
//                    }
//                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });


        _btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onclick", "urlStr ");

                mListener.takePhoto();
            }
        });


        _btnSubmitMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitMeetingDetails();

/*
                try {
                    submitMeetDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
*/
//                submitMeetingDetailsOld();

               /* RequestQueue queue = Volley.newRequestQueue(getActivity());

                String url = Const.Url.fullPath(Const.Url.update);
                Log.d("volley", "urlStr "+url);


                Log.i("url:", ":" + url);


                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley", response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley", error.getMessage());

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                        params.put(Const.Params.attendanceCount, "19");
                        params.put(Const.Params.lastMeetingDetails," Last Meet ");
                        params.put(Const.Params.meetingStatus,"1");
                        params.put(Const.Params.startTime,"10:00");
                        params.put(Const.Params.endTime,"11:00");
                        params.put(Const.Params.activeDate,"12");
//                params.put(Const.Params.latitude, String.format("%f",mCurrentLocation.getLatitude()));
//                params.put(Const.Params.longitude,String.format("%f",mCurrentLocation.getLongitude()));
                        params.put(Const.Params.latitude, "10.7812");
                        params.put(Const.Params.longitude,"27.412");

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {

                        String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;

                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put(Const.Value.HttpHeader.accept,Const.Value.HttpHeader.appJson);
                        headers.put(Const.Value.HttpHeader.authorization,authorizationValue);

                        return headers;
                    }
                };

                queue.add(stringRequest);



               /* JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        urlStr, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("volley", response.toString());
                                //pDialog.hide();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("volley", "Error: " + error.getMessage());
                        //pDialog.hide();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                        params.put(Const.Params.attendanceCount, "19");
                        params.put(Const.Params.lastMeetingDetails," Last Meet ");
                        params.put(Const.Params.meetingStatus,"1");
                        params.put(Const.Params.startTime,"10:00");
                        params.put(Const.Params.endTime,"11:00");
                        params.put(Const.Params.activeDate,"12");
//                params.put(Const.Params.latitude, String.format("%f",mCurrentLocation.getLatitude()));
//                params.put(Const.Params.longitude,String.format("%f",mCurrentLocation.getLongitude()));
                        params.put(Const.Params.latitude, "10.7812");
                        params.put(Const.Params.longitude,"27.412");

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {

                        String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;

                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put(Const.Value.HttpHeader.accept,Const.Value.HttpHeader.appJson);
                        headers.put(Const.Value.HttpHeader.authorization,authorizationValue);

                        return headers;
                    }

                };*/

            }
        });


       /*_btnSubmitMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //meetingId=9&meetingDescription=ok&attendance_count=1,2,3&activeDate=1&lat=1&longt=10&start_time=10&end_time=11&meetingStatus=1&meetingImage&last_meeting_details=text content
//                File file = new File(mImageFilePath);
                RequestParams params = new RequestParams();

                String[] listItems = SMMember.extractNames(mMembers);

                String urlStr = Const.Url.fullPath(Const.Url.update);
                params.put(Const.Params.meetingId,mMeetingId);

                params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                params.put(Const.Params.attendanceCount, "19");
                params.put(Const.Params.lastMeetingDetails," Last Meet ");
                params.put(Const.Params.meetingStatus,"1");
                params.put(Const.Params.startTime,"10:00");
                params.put(Const.Params.endTime,"11:00");
                params.put(Const.Params.activeDate,"12");
//                params.put(Const.Params.latitude, String.format("%f",mCurrentLocation.getLatitude()));
//                params.put(Const.Params.longitude,String.format("%f",mCurrentLocation.getLongitude()));
                params.put(Const.Params.latitude, "10.7812");
                params.put(Const.Params.longitude,"27.412");

//                params.put(Const.Params.meetingImage,file);
                final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Submitting...");
                progressDialog.show();

                AsyncHttpClient client = new AsyncHttpClient();
                Util.addSMIDHeader(client);
                client.post(urlStr, params, new StringHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Map<String, List<String>> headers, String content) {
                        Log.d("updateUI", "onSuccess: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);
                        progressDialog.dismiss();
                        if (content.length() > 0) {
                            try {
                                final JSONObject object = new JSONObject(content);

                                if (object.has(Const.Params.success) ) {
                                    if (object.getBoolean(Const.Params.success) == true && object.has(Const.Params.data)) {

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //onRetrievingEvents(object);
                                                Toast.makeText(getContext(), "Submitted successfully", Toast.LENGTH_LONG).show();
                                                onSubmittedSuccessfully();
                                            }
                                        });

                                    }

                                }
//                        DataHolder.getInstance().fitUser.addEntries(object);
//                        String status = object.getString(Const.Params.status);
//                        Log.d("updateUI", "onSuccess: " + object.getString(Const.Params.status) );
                                //redirectToIput(status);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Map<String, List<String>> headers, String content) {
                        // Server responded with a status code 4xx or 5xx error

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to submit", Toast.LENGTH_SHORT).show();

//                        onLoginFailed();
                            }
                        });

                        Log.d("updateUI", "onFailure: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        // An exception occurred during the request. Usually unable to connect or there was an error reading the response
                        Log.d("updateUI", "onFailure: "+throwable.getLocalizedMessage() );
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to submit", Toast.LENGTH_SHORT).show();

//                        onLoginFailed();
                            }
                        });
                    }
                });

            }
        });*/

    }


    private void submitMeetingDetails() {

        String urlStr = Const.Url.fullPath(Const.Url.update);

//        Map<String, String> headers = new HashMap<String, String>();
//        String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;
//        //headers.put(Const.Value.HttpHeader.accept,Const.Value.HttpHeader.appJson);
//        headers.put(Const.Value.HttpHeader.authorization,authorizationValue);
////        headers.put("Content-Type", "multipart/form-data"); //application/multipart
//        headers.put("Content-Type", "application/multipart");
//        headers.put("Accept", Const.Value.HttpHeader.appJson);


        VolleyMultipartRequest request = new VolleyMultipartRequest( Request.Method.POST ,urlStr, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    Log.d("httpResponse",result.toString());

//                    String status = result.getString("status");
//                    String message = result.getString("message");

                    /*if (status.equals(Constant.REQUEST_SUCCESS)) {
                        // tell everybody you have succed upload image and post strings
                        Log.i("Messsage", message);
                    } else {
                        Log.i("Unexpected", message);
                    } */
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                //error.printStackTrace();
            }
        }

        ) {

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> headers = new HashMap<String, String>();
                String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;
                headers.put(Const.Value.HttpHeader.authorization,authorizationValue);
                headers.put("Accept", Const.Value.HttpHeader.appJson);
                Log.d("simpleMulti"," submitMeetingDetails header "+headers.toString());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                Location currentLocation = DataHolder.getInstance().currentLocation;

                params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                params.put(Const.Params.attendanceCount, String.format("%d",mAttendedMembers.size()));
                params.put(Const.Params.lastMeetingDetails,_etLastMeetDescription.getText().toString());
                params.put(Const.Params.meetingStatus,"1");
                params.put(Const.Params.startTime,"10:00");
                params.put(Const.Params.endTime,"11:00");
                params.put(Const.Params.activeDate,"12");
                params.put(Const.Params.latitude, Double.toString(currentLocation.getLatitude()));
                params.put(Const.Params.longitude, Double.toString(currentLocation.getLongitude()));
                params.put(Const.Params.meetingId,mMeetingId);
                Log.d("simpleMulti"," params "+params.toString());
                return params;
            }

            @Override
            protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("meetingImage", new DataPart("meeting-image.jpg", AppHelper.getFileDataFromDrawable(getContext(), _capturedImage.getDrawable()), "image/jpeg"));
                params.put("params", new DataPart(mImageFilePath, "group_image".getBytes(), "image/*"));

                Log.d("simpleMulti"," ByteData "+params.toString());
                return params;
            }

        };


        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);





    }

    private void submitMeetDetails() throws JSONException {
        String urlStr = Const.Url.fullPath(Const.Url.update);

        String groupImage = Util.ImageToString(mBitMap);

        Map<String, String>  params = new HashMap<String, String>();
        params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
        params.put(Const.Params.attendanceCount, "19");
        params.put(Const.Params.lastMeetingDetails," Last Meet ");
        params.put(Const.Params.meetingStatus,"1");
        params.put(Const.Params.startTime,"10:00");
        params.put(Const.Params.endTime,"11:00");
        params.put(Const.Params.activeDate,"12");
        params.put(Const.Params.latitude, "10.7812");
        params.put(Const.Params.longitude,"27.412");


        JSONObject object = new JSONObject();
        object.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
        object.put(Const.Params.attendanceCount, "19");
        object.put(Const.Params.lastMeetingDetails," Last Meet ");
        object.put(Const.Params.meetingStatus,"1");
        object.put(Const.Params.startTime,"10:00");
        object.put(Const.Params.endTime,"11:00");
        object.put(Const.Params.activeDate,"12");
        object.put(Const.Params.latitude, "10.7812");
        object.put(Const.Params.longitude,"27.412");
//        object.put(Const.Params.meetingImage, groupImage);


        final String requestBody = params.toString();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,urlStr,object,

//        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, urlStr,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response:: ", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response",""+error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> headers = new HashMap<String, String>();
                String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;
                //headers.put(Const.Value.HttpHeader.accept,Const.Value.HttpHeader.appJson);
                headers.put(Const.Value.HttpHeader.authorization,authorizationValue);
//                headers.put("Content-Type", "multipart/form-data");
                headers.put("Accept", Const.Value.HttpHeader.appJson);

                Log.d("simpleMulti"," submitMeetDetails header "+headers.toString());
                return headers;
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                params.put(Const.Params.attendanceCount, "19");
                params.put(Const.Params.lastMeetingDetails," Last Meet ");
                params.put(Const.Params.meetingStatus,"1");
                params.put(Const.Params.startTime,"10:00");
                params.put(Const.Params.endTime,"11:00");
                params.put(Const.Params.activeDate,"12");
                params.put(Const.Params.latitude, "10.7812");
                params.put(Const.Params.longitude,"27.412");
                Log.d("simpleMulti","submitMeetDetails params "+params.toString());

                return params;
            }

            @Override
            public String getBodyContentType() {
                Log.d("simpleMulti","submitMeetDetails getBodyContentType ");

                return "application/json; charset=utf-8";
            }



        };


        VolleySingleton.getInstance(getContext()).addToRequestQueue(postRequest);

    }



   /* private void submitMeetingDetails() {

        String urlStr = Const.Url.fullPath(Const.Url.update);

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, urlStr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //onRetrievingEvents(object);
                                Log.d("Response", response);
                                Toast.makeText(getActivity(), " SMR Success", Toast.LENGTH_LONG).show();
                                onSubmittedSuccessfully();
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //onRetrievingEvents(object);
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                        onSubmittedSuccessfully();
                    }
                });
            }



        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put(Const.Params.meetingId,mMeetingId);
                params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
                params.put(Const.Params.attendanceCount, "19");
                params.put(Const.Params.lastMeetingDetails," Last Meet ");
                params.put(Const.Params.meetingStatus,"1");
                params.put(Const.Params.startTime,"10:00");
                params.put(Const.Params.endTime,"11:00");
                params.put(Const.Params.activeDate,"12");
                params.put(Const.Params.latitude, "10.7812");
                params.put(Const.Params.longitude,"27.412");
                Log.d("simpleMulti"," params "+params.toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                // Posting parameters to login url
                Map<String, String> headers = new HashMap<String, String>();
                String authorizationValue = Const.Value.HttpHeader.Bearer + DataHolder.getInstance().token;
                //headers.put(Const.Value.HttpHeader.accept,Const.Value.HttpHeader.appJson);
                headers.put(Const.Value.HttpHeader.authorization,authorizationValue);
                headers.put("Content-Type", "multipart/form-data");
                headers.put("Accept", Const.Value.HttpHeader.appJson);

                Log.d("simpleMulti"," header "+headers.toString());
                return headers;
            }
        };


//        Map<String, String> headers = new HashMap<String, String>();
//
//        smr.setHeaders(headers);



        request.addFile(Const.Params.meetingImage, mImageFilePath);

        //smr.addMultipartParam(Const.Params.meetingDescription, "text/plain",_etMeetingDiscussion.getText().toString());

        request.addStringParam(Const.Params.meetingDescription, " THis is meeting descripton");
//        smr.addMultipartParam(Const.Params.meetingDescription, "text/plain", "some text");

//        smr.addStringParam(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
//        smr.addStringParam(Const.Params.attendanceCount, "19");
//        smr.addStringParam(Const.Params.lastMeetingDetails," Last Meet ");
//        smr.addStringParam(Const.Params.meetingStatus,"1");
//        smr.addStringParam(Const.Params.startTime,"10:00");
//        smr.addStringParam(Const.Params.endTime,"11:00");
//        smr.addStringParam(Const.Params.activeDate,"12");
//        smr.addStringParam(Const.Params.latitude, "10.7812");
//        smr.addStringParam(Const.Params.longitude,"27.412");


        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity());
        mRequestQueue.add(request);
        mRequestQueue.start();

    } */

    private void submitMeetingDetailsOld() {

        //meetingId=9&meetingDescription=ok&attendance_count=1,2,3&activeDate=1&lat=1&longt=10&start_time=10&end_time=11&meetingStatus=1&meetingImage&last_meeting_details=text content
//                File file = new File(mImageFilePath);
        RequestParams params = new RequestParams();

        String[] listItems = SMMember.extractNames(mMembers);

        String urlStr = Const.Url.fullPath(Const.Url.update);
        params.put(Const.Params.meetingId,mMeetingId);

        params.put(Const.Params.meetingDescription, _etMeetingDiscussion.getText().toString());
        params.put(Const.Params.attendanceCount, "19");
        params.put(Const.Params.lastMeetingDetails," Last Meet ");
        params.put(Const.Params.meetingStatus,"1");
        params.put(Const.Params.startTime,"10:00");
        params.put(Const.Params.endTime,"11:00");
        params.put(Const.Params.activeDate,"12");
//                params.put(Const.Params.latitude, String.format("%f",mCurrentLocation.getLatitude()));
//                params.put(Const.Params.longitude,String.format("%f",mCurrentLocation.getLongitude()));
        params.put(Const.Params.latitude, "10.7812");
        params.put(Const.Params.longitude,"27.412");

//                params.put(Const.Params.meetingImage,file);
        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Submitting...");
        progressDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        Util.addSMIDHeader(client);
        client.setHeader(Const.Value.HttpHeader.contentType,Const.Value.HttpHeader.multipartForm);

        client.post(urlStr, params, new StringHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Map<String, List<String>> headers, String content) {
                Log.d("updateUI", "onSuccess: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);
                progressDialog.dismiss();
                if (content.length() > 0) {
                    try {
                        final JSONObject object = new JSONObject(content);

                        if (object.has(Const.Params.success) ) {
                            if (object.getBoolean(Const.Params.success) == true && object.has(Const.Params.data)) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //onRetrievingEvents(object);
                                        Toast.makeText(getContext(), "Submitted successfully", Toast.LENGTH_LONG).show();
                                        onSubmittedSuccessfully();
                                    }
                                });

                            }

                        }
//                        DataHolder.getInstance().fitUser.addEntries(object);
//                        String status = object.getString(Const.Params.status);
//                        Log.d("updateUI", "onSuccess: " + object.getString(Const.Params.status) );
                        //redirectToIput(status);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Map<String, List<String>> headers, String content) {
                // Server responded with a status code 4xx or 5xx error

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to submit", Toast.LENGTH_SHORT).show();

//                        onLoginFailed();
                    }
                });

                Log.d("updateUI", "onFailure: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);

            }

            @Override
            public void onFailure(Throwable throwable) {
                // An exception occurred during the request. Usually unable to connect or there was an error reading the response
                Log.d("updateUI", "onFailure: "+throwable.getLocalizedMessage() );
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to submit", Toast.LENGTH_SHORT).show();

//                        onLoginFailed();
                    }
                });
            }
        });

    }


    private void onSubmittedSuccessfully() {
        getActivity().finish();
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



    private void retrieveMembers() {
        String meetingId = mMeetingId;
        String urlStr = Const.Url.fullPath(Const.Url.tlfMembers,meetingId);
        RequestParams params = new RequestParams();
        Log.d("updateUI",urlStr + "\n Params : " + params.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        Util.addSMIDHeader(client);
        client.get(urlStr, params, new StringHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Map<String, List<String>> headers, String content) {
                /* Request was successful */
                Log.d("updateUI", "onSuccess: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);
                if (content.length() > 0) {
                    try {
                        JSONObject object = new JSONObject(content);

                        if (object.has(Const.Params.success) ) {
                            if (object.getBoolean(Const.Params.success) == true && object.has(Const.Params.data)) {
                                onRetrievingMembers(object);

                            }

                        }
//                        DataHolder.getInstance().fitUser.addEntries(object);
//                        String status = object.getString(Const.Params.status);
//                        Log.d("updateUI", "onSuccess: " + object.getString(Const.Params.status) );
                        //redirectToIput(status);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Map<String, List<String>> headers, String content) {
                /* Server responded with a status code 4xx or 5xx error */

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        progressDialog.dismiss();
//                        onLoginFailed();
                    }
                });

                Log.d("updateUI", "onFailure: " + statusCode + "  Response: " +headers.toString() + " Content:"+content);

            }

            @Override
            public void onFailure(Throwable throwable) {
                /* An exception occurred during the request. Usually unable to connect or there was an error reading the response */
                Log.d("updateUI", "onFailure: "+throwable.getLocalizedMessage() );
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        progressDialog.dismiss();
//                        onLoginFailed();
                    }
                });
            }
        });


    }

    private void onRetrievingMembers(JSONObject resObject) {

        mMembers =  SMMember.parseList(resObject);
        Log.d("result", "mMembers count: "+ String.format("%d",mMembers.size()));



    }


    public void onImageCaptured(String filePath, Uri photoUri) {

        _capturedImage.setVisibility(View.VISIBLE);
        mImageFilePath = filePath;
        Uri imageUri = Uri.parse(filePath);
        _capturedImage.setImageURI(imageUri);

        try {
            mBitMap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

//        Glide.with(getView()).load(imageUri).into(_capturedImage);

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("onLoc", String.format("Locations : %f %", location.getLatitude(), location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void takePhoto();

    }
}
