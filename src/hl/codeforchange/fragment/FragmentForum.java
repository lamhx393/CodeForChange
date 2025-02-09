package hl.codeforchange.fragment;

import hl.codeforchange.activity.R;
import hl.codeforchange.adapter.ForumAdapter;
import hl.codeforchange.item.ItemForum;
import hl.codeforchange.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class FragmentForum extends SherlockFragment implements OnClickListener {
	private static final String TAG = "FragmentHome";
	private PullToRefreshListView mPullRefreshListViewForum;
	private View v;
	private ListView lvForum;
	private final ArrayList<ItemForum> itemForum = new ArrayList<ItemForum>();
	private ForumAdapter adapter;

	private Button btnPost, btnImage;
	private EditText edStatusPost;
	private String strPathImagePost = "";

	Uri imageUri = null;

	public static FragmentForum newInstance(int position) {
		final FragmentForum f = new FragmentForum();
		final Bundle args = new Bundle();
		args.putInt("pos", position);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDataForum();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_forum, container, false);
		mPullRefreshListViewForum = (PullToRefreshListView) v
				.findViewById(R.id.pull_refresh_forum);

		mPullRefreshListViewForum
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel("waitting ... ");
						new RefreshData().execute();
					}
				});

		lvForum = mPullRefreshListViewForum.getRefreshableView();

		registerForContextMenu(lvForum);

		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.header_forum,
				lvForum, false);
		lvForum.addHeaderView(header);

		btnImage = (Button) v.findViewById(R.id.btnPostImage);
		btnPost = (Button) v.findViewById(R.id.btnPostStatus);
		edStatusPost = (EditText) v.findViewById(R.id.edPostStatus);

		btnImage.setOnClickListener(this);
		btnPost.setOnClickListener(this);
		adapter = new ForumAdapter(getActivity(), itemForum, getResources());

		lvForum.setAdapter(adapter);

		return v;
	}

	public void getDataForum() {
		// itemForum = new ArrayList<ItemForum>();
		String strValue = Utils.readData(getResources(), R.raw.data_post_forum);
		JSONObject jsonRoot;
		try {
			jsonRoot = new JSONObject(strValue);
			Gson gson = new Gson();
			JSONArray jsonArray = jsonRoot.getJSONArray("forum_post");
			for (int i = 0; i < jsonArray.length(); i++) {
				Log.d("JSON", jsonArray.getJSONObject(i).toString());
				JSONObject objGson = jsonArray.getJSONObject(i);
				final ItemForum course = gson.fromJson(objGson.toString(),
						ItemForum.class);
				itemForum.add(course);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class RefreshData extends AsyncTask<Void, Void, ListView> {

		@Override
		protected ListView doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
				// getDataForum();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(ListView result) {
			// adapter.notifyDataSetChanged();
			mPullRefreshListViewForum.onRefreshComplete();
			super.onPostExecute(result);
		}

	}

	@Override
	public void onResume() {

		super.onResume();
	}

	private void capturePicture(Context context, int requestCode) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, timeStamp);
		imageUri = context.getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// imageUri = getImageFileUri();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, requestCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == FragmentActivity.RESULT_OK) {
			Cursor c = null;
			String[] projection = { MediaStore.MediaColumns._ID,
					MediaStore.Images.ImageColumns.ORIENTATION,
					MediaStore.Images.Media.DATA };

			c = getActivity().getContentResolver().query(imageUri, projection,
					null, null, null);
			int columnIndex = c
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			c.moveToFirst();
			Log.i("PATH FILE  ", c.getString(columnIndex));

			strPathImagePost = c.getString(c
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnPostImage) {
			capturePicture(getActivity(), 100);
		}
		if (id == R.id.btnPostStatus
				&& !edStatusPost.getText().toString().equals("")
				&& !strPathImagePost.equals("")) {

			String timeStamp = new SimpleDateFormat("dd-MM-yyyy")
					.format(new Date());
			ItemForum itemF = new ItemForum();
			itemF.setUser_name(getString(R.string.user_name_test));
			itemF.setStatus(edStatusPost.getText().toString());
			itemF.setPic_post("file://" + strPathImagePost);
			itemF.setDate(timeStamp);
			edStatusPost.setText("");

			itemForum.add(0, itemF);
			// itemForum.add(itemF);
			adapter.notifyDataSetChanged();
			lvForum.setAdapter(adapter);
		} else {
			Toast.makeText(getActivity(), "Chưa có hình ảnh. Kiểm tra lại! ",
					Toast.LENGTH_SHORT).show();
		}

	}

}
