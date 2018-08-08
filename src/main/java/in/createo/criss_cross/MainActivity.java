package in.createo.criss_cross;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View.OnDragListener;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.widget.TextView;

public class MainActivity extends Activity {

    private RelativeLayout parent_layout;
    private JSONArray json;
    private ArrayList<ArrayList<Integer>> reference_matrix = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> combinational_matrix = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> reference_matrix_copy = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> combinational_matrix_copy = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<String>> ones=new ArrayList<ArrayList<String>>();
    private int v1, v2, json_data_size;
    private int number_of_nodes = 0, current_data = 0;
    private int fill_radius, cavity_radius, box_size, box_stroke;
    private float current_y,start_x,start_y;
    private int matched = 0;
    private File storage;

    private ArrayList<ArrayList<Float>> co_ordinates = new ArrayList<ArrayList<Float>>();
    private ArrayList<String> edges_names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        File dir=getApplicationContext().getDir("",Context.MODE_PRIVATE);
        storage=new File(dir, "storage.txt");
        try
        {
            FileInputStream fis=new FileInputStream(storage);
            DataInputStream dis=new DataInputStream(fis);
            BufferedReader br=new BufferedReader(new InputStreamReader(dis));
            String str,data="";
            while((str=br.readLine())!=null)
                data+=str;
            if(!data.equals("")) {
                current_data = Integer.valueOf(data);
            }
            dis.close();
        }catch(Exception e){ }

        findViewById(R.id.help_parent).setVisibility(View.GONE);
        findViewById(R.id.solution_parent).setVisibility(View.GONE);

        findViewById(R.id.button_done).setOnClickListener(new MyDoneClickListener());
        findViewById(R.id.button_done).setAlpha(0.5f);

        findViewById(R.id.button_help).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_help_close).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_solution).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_solution_close).setOnClickListener(new MyHelpSolutionClickListener());

        initialize_matrix();

        find_answer();

        draw_graph();
    }

    void draw_graph()
    {
        parent_layout = findViewById(R.id.parent_layout);
        fill_radius = getResources().getInteger(R.integer.node_radius_size);
        cavity_radius = getResources().getInteger(R.integer.cavity_radius_size);
        box_size = getResources().getInteger(R.integer.box_width_height_size);
        box_stroke= getResources().getInteger(R.integer.box_stroke_size);
        int button_height=getResources().getInteger(R.integer.button_height_size);
        int button_width=getResources().getInteger(R.integer.button_width_size);

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        int width_parent_layout = display.widthPixels;
        int height_parent_layout = display.heightPixels;
        int scaling_factor=(int)Math.floor(((height_parent_layout)-(button_height))/(number_of_nodes * 2 *box_size));
        fill_radius =fill_radius*(scaling_factor);
        cavity_radius *=(scaling_factor);
        box_size *=(scaling_factor);
        box_stroke *=(scaling_factor);
        float center_x = width_parent_layout / (float) 2.0;
        float center_y = height_parent_layout / (float) 2.0;
        float width_height = (float) ((number_of_nodes) * box_size);
        start_x = center_x - width_height+ (2*box_size);
        start_y = center_y - width_height+(button_height/2);

        LinearLayout l = findViewById(R.id.solution);
        l.setY(center_y - (button_width/2));
        l.setX(center_x - ((3*button_width)/2));

        for (int i = 1; i <= number_of_nodes; i++)
            for (int j = 1; j <= number_of_nodes; j++)
            {
                if (reference_matrix.get(i).get(j).toString().equals("1")) {
                    LinearLayout new_layer=new LinearLayout(MainActivity.this);
                    LayoutParams para = new LayoutParams(2*box_size,2*box_size);
                    new_layer.setLayoutParams(para);
                    new_layer.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.box));
                    GradientDrawable gd=(GradientDrawable) new_layer.getBackground();
                    gd.setStroke(4*box_stroke,  getResources().getColor(R.color.colorPrimaryDark));
                    String tag="cavity_"+Integer.toString(i)+"_"+Integer.toString(j);
                    new_layer.setTag(tag);
                    new_layer.setGravity(Gravity.CENTER);

                    ImageView new_node = new ImageView(MainActivity.this);
                    para = new LayoutParams(2*cavity_radius, 2 * cavity_radius);
                    new_node.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.cavity));
                    gd=(GradientDrawable) new_node.getBackground();
                    gd.setStroke(4*box_stroke,  getResources().getColor(R.color.colorEdge));
                    gd.setCornerRadius(cavity_radius);
                    new_node.setLayoutParams(para);
                    new_layer.addView(new_node);

                    float x = start_x+((i-1)*2*box_size)-((i-1)*4*box_stroke);
                    new_layer.setX(x);
                    float y = start_y+((j-1)*2*box_size)-((j-1)*4*box_stroke);
                    new_layer.setY(y);

                    parent_layout.addView(new_layer);
                }
                else {
                    LinearLayout new_layer=new LinearLayout(MainActivity.this);
                    LayoutParams para = new LayoutParams(2*box_size,2*box_size);
                    new_layer.setLayoutParams(para);
                    new_layer.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.box));
                    GradientDrawable gd=(GradientDrawable) new_layer.getBackground();
                    gd.setStroke(4*box_stroke,  getResources().getColor(R.color.colorPrimaryDark));
                    String tag="cavity_"+Integer.toString(i)+"_"+Integer.toString(j);
                    new_layer.setTag(tag);
                    new_layer.setGravity(Gravity.CENTER);

                    float x = start_x+((i-1)*2*box_size)-((i-1)*4*box_stroke);
                    new_layer.setX(x);
                    float y = start_y+((j-1)*2*box_size)-((j-1)*4*box_stroke);
                    new_layer.setY(y);

                    parent_layout.addView(new_layer);
                }
            }

        for (int i = 1; i <= number_of_nodes; i++)
            for (int j = 1; j <= number_of_nodes; j++)
            {
                if (combinational_matrix.get(i).get(j).toString().equals("1")) {
                    LinearLayout new_layer=new LinearLayout(MainActivity.this);
                    LayoutParams para = new LayoutParams(2*box_size,2*box_size);
                    new_layer.setLayoutParams(para);
                    new_layer.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.box));
                    GradientDrawable gd=(GradientDrawable) new_layer.getBackground();
                    gd.setStroke(4*box_stroke,  getResources().getColor(R.color.colorPrimaryDark));
                    String tag="fill_"+Integer.toString(i)+"_"+Integer.toString(j);
                    new_layer.setTag(tag);
                    new_layer.setGravity(Gravity.CENTER);

                    ImageView new_node = new ImageView(MainActivity.this);
                    para = new LayoutParams(2*fill_radius, 2 * fill_radius);
                    if(reference_matrix.get(i).get(j).toString().equals("1"))
                        new_node.setImageResource(R.mipmap.safe);
                    else
                        new_node.setImageResource(R.mipmap.unsafe);
                    //new_node.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.fill));
                    tag="image_"+Integer.toString(i)+"_"+Integer.toString(j);
                    ArrayList<String> temp=new ArrayList<>(ones.get(i));
                    temp.set(j,tag);
                    ones.set(i,temp);
                    new_node.setTag(tag);
                    new_node.setLayoutParams(para);
                    new_layer.addView(new_node);

                    float x = start_x+((i-1)*2*box_size)-((i-1)*4*box_stroke);
                    new_layer.setX(x);
                    float y = start_y+((j-1)*2*box_size)-((j-1)*4*box_stroke);
                    new_layer.setY(y);

                    parent_layout.addView(new_layer);
                }
                else {
                    LinearLayout new_layer=new LinearLayout(MainActivity.this);
                    LayoutParams para = new LayoutParams(2*box_size,2*box_size);
                    new_layer.setLayoutParams(para);
                    new_layer.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.box));
                    GradientDrawable gd=(GradientDrawable) new_layer.getBackground();
                    gd.setStroke(4*box_stroke,  getResources().getColor(R.color.colorPrimaryDark));
                    String tag="fill_"+Integer.toString(i)+"_"+Integer.toString(j);
                    new_layer.setTag(tag);
                    new_layer.setGravity(Gravity.CENTER);

                    float x = start_x+((i-1)*2*box_size)-((i-1)*4*box_stroke);
                    new_layer.setX(x);
                    float y = start_y+((j-1)*2*box_size)-((j-1)*4*box_stroke);
                    new_layer.setY(y);

                    parent_layout.addView(new_layer);
                }
            }

        for(int i=1;i<=number_of_nodes;i++)
        {
            LinearLayout new_layer=new LinearLayout(MainActivity.this);
            LayoutParams para = new LayoutParams(2*box_size,2*box_size);
            new_layer.setLayoutParams(para);
            new_layer.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nodes));
            String tag="node"+i;
            new_layer.setTag(tag);
            new_layer.setGravity(Gravity.CENTER);

            ImageView new_node = new ImageView(MainActivity.this);
            para = new LayoutParams(2*fill_radius, 2 * fill_radius);
            String img="node"+i;
            int img_id=getResources().getIdentifier(img, "mipmap", getPackageName());
            new_node.setImageResource(img_id);
            //new_node.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.fill));
            new_node.setLayoutParams(para);
            new_layer.addView(new_node);

            new_layer.setOnLongClickListener(new MyClickListener());
            new_layer.setOnDragListener(new MyDragListener());

            float x = start_x-(2*box_size);
            new_layer.setX(x);
            float y = start_y+((i-1)*2*box_size)-((i-1)*4*box_stroke);
            new_layer.setY(y);

            parent_layout.addView(new_layer);
        }
    }

    void initialize_matrix()
    {
        String json_str = null;
        reference_matrix.clear();
        combinational_matrix.clear();
        combinational_matrix_copy.clear();
        reference_matrix_copy.clear();
        ones.clear();
        try {
            InputStream is = getAssets().open("data.json");
            int size = is.available();
            byte buffer[] = new byte[size];
            is.read(buffer);
            is.close();
            json_str = new String(buffer, "UTF-8");

            json = new JSONArray(json_str);
            json_data_size=json.length();
            number_of_nodes = json.getJSONObject(current_data).getInt("no_nodes");
            JSONArray json_arr = (json.getJSONObject(current_data).getJSONArray("reference_matrix"));
            ArrayList<Integer> header = new ArrayList<>();
            ArrayList<Integer> zeros = new ArrayList<>();
            ArrayList<String> empty= new ArrayList<>();
            for (int i = 0; i <= number_of_nodes; i++) {
                header.add(i);
                zeros.add(0);
                empty.add("");
            }
            reference_matrix.add((ArrayList<Integer>) header.clone());
            reference_matrix_copy.add((ArrayList<Integer>) header.clone());
            ones.add((ArrayList<String>) empty.clone());
            for (int i = 0; i < json_arr.length(); i++) {
                ArrayList<Integer> inner_arr = new ArrayList<>(zeros);
                inner_arr.set(0, i + 1);
                JSONArray inner_arr_json = json_arr.getJSONArray(i);
                for (int j = 0; j < inner_arr_json.length(); j++)
                    inner_arr.set(inner_arr_json.getInt(j), 1);
                reference_matrix.add((ArrayList<Integer>) inner_arr.clone());
                reference_matrix_copy.add((ArrayList<Integer>) inner_arr.clone());
                ones.add((ArrayList<String>) empty.clone());
            }
            json_arr = (json.getJSONObject(current_data).getJSONArray("combinational_matrix"));
            combinational_matrix.add((ArrayList<Integer>) header.clone());
            combinational_matrix_copy.add((ArrayList<Integer>) header.clone());
            for (int i = 0; i < json_arr.length(); i++) {
                ArrayList<Integer> inner_arr = new ArrayList<>(zeros);
                inner_arr.set(0, i + 1);
                JSONArray inner_arr_json = json_arr.getJSONArray(i);
                for (int j = 0; j < inner_arr_json.length(); j++)
                    inner_arr.set(inner_arr_json.getInt(j), 1);
                combinational_matrix.add((ArrayList<Integer>) inner_arr.clone());
                combinational_matrix_copy.add((ArrayList<Integer>) inner_arr.clone());
            }
        } catch (Exception e) { }
    }

    void rotate()
    {
        if(v1!=v2) {
            findViewById(R.id.button_done).setAlpha(0.5f);
            matched=0;
            int index1 = 0, index2 = 0;
            for (int i = 1; i <= number_of_nodes; i++) {
                if (combinational_matrix.get(0).get(i).toString().equals(Integer.toString(v1)))
                    index1 = i;
                else if (combinational_matrix.get(0).get(i).toString().equals(Integer.toString(v2)))
                    index2 = i;
            }
            for (int i = 0; i <= number_of_nodes; i++) {
                ArrayList<Integer> temp_arr = combinational_matrix.get(i);
                int temp = temp_arr.get(index1);
                temp_arr.set(index1, temp_arr.get(index2));
                temp_arr.set(index2, temp);
                combinational_matrix.set(i, temp_arr);

                ArrayList<String> temp_arr_ones = ones.get(i);
                String temp_str = temp_arr_ones.get(index1);
                temp_arr_ones.set(index1, temp_arr_ones.get(index2));
                temp_arr_ones.set(index2, temp_str);
                ones.set(i, temp_arr_ones);

                if (i > 0) {
                    String tags = "fill_" + Integer.toString(i) + "_" + Integer.toString(v1);
                    float y = start_y + ((index2 - 1) * 2 * box_size) - ((index2 - 1) * 4 * box_stroke);
                    LinearLayout linearLayout1 = parent_layout.findViewWithTag(tags);
                    linearLayout1.setY(y);
                    tags = "fill_" + Integer.toString(i) + "_" + Integer.toString(v2);
                    y = start_y + ((index1 - 1) * 2 * box_size) - ((index1 - 1) * 4 * box_stroke);
                    LinearLayout linearLayout2 = parent_layout.findViewWithTag(tags);
                    linearLayout2.setY(y);
                    String image_tag="";
                    if (combinational_matrix.get(i).get(index2).toString().equals("1")) {
                        image_tag = ones.get(i).get(index2);
                        ImageView iv=parent_layout.findViewWithTag(image_tag);
                        if (reference_matrix.get(i).get(index2).toString().equals("1"))
                            iv.setImageResource(R.mipmap.safe);
                        else
                            iv.setImageResource(R.mipmap.unsafe);
                    }
                    if (combinational_matrix.get(i).get(index1).toString().equals("1")) {
                        image_tag = ones.get(i).get(index1);
                        ImageView iv=parent_layout.findViewWithTag(image_tag);
                        if (reference_matrix.get(i).get(index1).toString().equals("1"))
                            iv.setImageResource(R.mipmap.safe);
                        else
                            iv.setImageResource(R.mipmap.unsafe);
                    }
                }
            }
            ArrayList<Integer> temp = combinational_matrix.get(index1);
            combinational_matrix.set(index1, combinational_matrix.get(index2));
            combinational_matrix.set(index2, temp);

            ArrayList<String> temp_arr = ones.get(index1);
            ones.set(index1, ones.get(index2));
            ones.set(index2, temp_arr);

            for (int i = 1; i <= number_of_nodes; i++) {
                String tags="fill_"+v1+"_"+i;
                float x=start_x+((index2-1)*2*box_size)-((index2-1)*4*box_stroke);
                LinearLayout linearLayout1=parent_layout.findViewWithTag(tags);
                linearLayout1.setX(x);
                tags="fill_"+v2+"_"+i;
                x=start_x+((index1-1)*2*box_size)-((index1-1)*4*box_stroke);
                LinearLayout linearLayout2=parent_layout.findViewWithTag(tags);
                linearLayout2.setX(x);
                if (combinational_matrix.get(index2).get(i).toString().equals("1")) {
                    String image_tag = ones.get(index2).get(i);
                    ImageView iv=parent_layout.findViewWithTag(image_tag);
                    if (reference_matrix.get(index2).get(i).toString().equals("1"))
                        iv.setImageResource(R.mipmap.safe);
                    else
                        iv.setImageResource(R.mipmap.unsafe);
                }
                if (combinational_matrix.get(index1).get(i).toString().equals("1")) {
                    String image_tag = ones.get(index1).get(i);
                    ImageView iv=parent_layout.findViewWithTag(image_tag);
                    if (reference_matrix.get(index1).get(i).toString().equals("1"))
                        iv.setImageResource(R.mipmap.safe);
                    else
                        iv.setImageResource(R.mipmap.unsafe);
                }
            }

            breakHere:for(int i=1;i<=number_of_nodes;i++) {
                for (int j = i + 1; j <= number_of_nodes; j++)
                {
                    if(reference_matrix.get(i).get(j).toString()!=combinational_matrix.get(i).get(j).toString())
                        break breakHere;
                }
                if(i==number_of_nodes)
                {
                    if(current_data<(json_data_size-1)) {
                        findViewById(R.id.button_done).setAlpha(1.0f);
                        matched = 1;
                    }
                }
            }
        }
    }

    void find_answer()
    {
        ArrayList<String> swap_history=new ArrayList<>();
        ArrayList<String> swap_history_vertex=new ArrayList<>();

        for(int i=1;i<number_of_nodes+1;i++)
        {
            ArrayList<Integer> sub_combinational_matrix=new ArrayList<>(combinational_matrix_copy.get(i).subList(1,number_of_nodes+1));
            ArrayList<Integer> sub_reference_matrix=new ArrayList<>(reference_matrix_copy.get(i).subList(1,number_of_nodes+1));
            if(!sub_combinational_matrix.toString().equals(sub_reference_matrix.toString()))
            {
                //find the row with the least mismatches
                int min_mismatch_row=0,index_min_mismatch_row=i;
                for(int j=0;j<sub_combinational_matrix.size();j++)
                {
                    if(!sub_combinational_matrix.get(j).toString().equals(sub_reference_matrix.get(j).toString()))
                        min_mismatch_row++;
                    if(min_mismatch_row>2)
                        break;
                }
                for(int j=i+1;j<number_of_nodes+1;j++) {
                    ArrayList<Integer> sub_combinational_matrix_new = new ArrayList<>(combinational_matrix_copy.get(j).subList(1, number_of_nodes + 1));
                    if (sub_combinational_matrix_new.toString().equals(sub_reference_matrix.toString()))
                    {
                        min_mismatch_row=0;
                        index_min_mismatch_row=j;
                        break;
                    }
                    else {
                        int temp_min_mismatch_row = 0;
                        for (int x = 0; x < sub_combinational_matrix_new.size(); x++) {
                            if (!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.toString()))
                                temp_min_mismatch_row++;
                            if (temp_min_mismatch_row > 2)
                                break;
                        }
                        if (temp_min_mismatch_row < min_mismatch_row)
                            index_min_mismatch_row = j;
                    }
                }

                //rotate index_min_mismatch_row and i
                if(index_min_mismatch_row!=i)
                {
                    String temp=Integer.toString(i)+Integer.toString(index_min_mismatch_row);
                    swap_history.add(temp);
                    temp=Integer.toString(index_min_mismatch_row)+Integer.toString(i);
                    swap_history.add(temp);
                    temp=combinational_matrix_copy.get(0).get(i).toString()+combinational_matrix_copy.get(0).get(index_min_mismatch_row).toString();
                    swap_history_vertex.add(temp);
                    temp=combinational_matrix_copy.get(0).get(index_min_mismatch_row).toString()+combinational_matrix_copy.get(0).get(i).toString();
                    swap_history_vertex.add(temp);

                    for (int x = 0; x <= number_of_nodes; x++) {
                        ArrayList<Integer> temp_arr = combinational_matrix_copy.get(x);
                        int temp_val = temp_arr.get(i);
                        temp_arr.set(i, temp_arr.get(index_min_mismatch_row));
                        temp_arr.set(index_min_mismatch_row, temp_val);
                        combinational_matrix_copy.set(x, temp_arr);
                    }
                    ArrayList<Integer> temp_arr = combinational_matrix_copy.get(i);
                    combinational_matrix_copy.set(i, combinational_matrix_copy.get(index_min_mismatch_row));
                    combinational_matrix_copy.set(index_min_mismatch_row, temp_arr);
                }

                //find the index where there is a mismatch, in the current row i
                int swap_column_index1=0,swap_column_index2=0;
                ArrayList<Integer> sub_combinational_matrix_new=new ArrayList<>(combinational_matrix_copy.get(i).subList(1,number_of_nodes+1));
                if(!sub_combinational_matrix_new.toString().equals(sub_reference_matrix.toString()))
                {
                    for(int x=0;x<sub_combinational_matrix_new.size();x++)
                    {
                        if(!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.get(x).toString()))
                        {
                            if(swap_column_index1==0)
                                swap_column_index1=x+1;
                            else
                                swap_column_index2=x+1;
                        }
                    }
                }
                if((swap_column_index1==0 || swap_column_index2==0) && (swap_column_index1!=0 || swap_column_index2!=0))
                    break;

                boolean index=false,vertex=false;
                if(swap_history.contains(Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2)))
                    index=true;
                if(swap_history_vertex.contains(combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString()))
                    vertex=true;

                //find some other row that closely matches the current row i
                boolean no_match=false;
                if(vertex && index && swap_column_index1!=0 && swap_column_index2!=0)
                {
                    swap_column_index1=i;
                    swap_column_index2=-1;
                    for(int j=i+1;j<number_of_nodes+1;j++)
                    {
                        sub_combinational_matrix_new=new ArrayList<>(combinational_matrix_copy.get(j).subList(1,number_of_nodes+1));
                        if(sub_reference_matrix.toString().equals(sub_combinational_matrix_new.toString()))
                        {
                            swap_column_index2=j;
                            break;
                        }
                        else
                        {
                            int temp_min_mismatch=0;
                            for(int x=0;x<sub_combinational_matrix_new.size();x++)
                            {
                                if(!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.get(x).toString()))
                                    temp_min_mismatch++;
                                if(temp_min_mismatch>2)
                                    break;
                            }
                            if(temp_min_mismatch==2 && swap_column_index2==-1)
                            {
                                swap_column_index2=j;
                                index=false;vertex=false;
                                if(swap_history.contains(Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2)))
                                    index=true;
                                if(swap_history_vertex.contains(combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString()))
                                    vertex=true;

                                if(vertex && index)
                                    swap_column_index2=-1;
                            }
                        }
                        if(j==number_of_nodes && swap_column_index2==-1)
                            no_match=true;
                    }
                    if(no_match)
                    {
                        if(i==number_of_nodes)
                            break;
                        else
                            continue;
                    }
                }

                if(swap_column_index2==-1)
                    break;

                String temp=Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2);
                swap_history.add(temp);
                temp=Integer.toString(swap_column_index2)+Integer.toString(swap_column_index1);
                swap_history.add(temp);
                temp=combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString();
                swap_history_vertex.add(temp);
                temp=combinational_matrix_copy.get(0).get(swap_column_index2).toString()+combinational_matrix_copy.get(0).get(swap_column_index1).toString();
                swap_history_vertex.add(temp);

                for (int x = 0; x <= number_of_nodes; x++) {
                    ArrayList<Integer> temp_arr = combinational_matrix_copy.get(x);
                    int temp_val = temp_arr.get(swap_column_index1);
                    temp_arr.set(swap_column_index1, temp_arr.get(swap_column_index2));
                    temp_arr.set(swap_column_index2, temp_val);
                    combinational_matrix_copy.set(x, temp_arr);
                }
                ArrayList<Integer> temp_arr = combinational_matrix_copy.get(swap_column_index1);
                combinational_matrix_copy.set(swap_column_index1, combinational_matrix_copy.get(swap_column_index2));
                combinational_matrix_copy.set(swap_column_index2, temp_arr);

                i=0;
            }
        }
    }

    private final class MyHelpSolutionClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(view==findViewById(R.id.button_help)) {
                parent_layout.setVisibility(View.GONE);
                findViewById(R.id.help_parent).setVisibility(View.VISIBLE);
            }
            else if(view==findViewById(R.id.button_help_close))
            {
                parent_layout.setVisibility(View.VISIBLE);
                findViewById(R.id.help_parent).setVisibility(View.GONE);
            }
            else if(view==findViewById(R.id.button_solution))
            {
                parent_layout.setVisibility(View.GONE);
                findViewById(R.id.solution_parent).setVisibility(View.VISIBLE);

                int s1=-1,s2=-1;
                for(int i=1;i<number_of_nodes+1;i++)
                {
                    if(!combinational_matrix.get(0).get(i).toString().equals(combinational_matrix_copy.get(0).get(i).toString()))
                    {
                        s1=combinational_matrix.get(0).get(i);
                        s2=combinational_matrix_copy.get(0).get(i);
                    }
                }

                int button_width=getResources().getInteger(R.integer.button_width_size);
                LinearLayout r=findViewById(R.id.solution);
                r.removeAllViews();
                if(s1!=-1 && s2 !=-1) {
                    ImageView i = new ImageView(MainActivity.this);
                    LayoutParams l = new LayoutParams(button_width, button_width);
                    i.setLayoutParams(l);
                    String img = "node" + s1;
                    int img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                    i = new ImageView(MainActivity.this);
                    l = new LayoutParams(button_width, button_width);
                    i.setLayoutParams(l);
                    img = "swap";
                    img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                    i = new ImageView(MainActivity.this);
                    l = new LayoutParams(button_width, button_width);
                    i.setLayoutParams(l);
                    img = "node" + s2;
                    img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                }
            }
            else if(view==findViewById(R.id.button_solution_close))
            {
                parent_layout.setVisibility(View.VISIBLE);
                findViewById(R.id.solution_parent).setVisibility(View.GONE);
            }
        }
    }

    private final class MyDoneClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(matched==1) {
                current_data++;

                try
                {
                    FileOutputStream fos=new FileOutputStream(storage);
                    fos.write(Integer.toString(current_data).getBytes());
                    fos.close();
                }catch (Exception e){}

                for (int i = 1; i <= number_of_nodes; i++) {
                    for(int j=1; j<=number_of_nodes;j++) {
                        parent_layout.removeView(parent_layout.findViewWithTag("cavity_"+Integer.toString(i)+"_"+Integer.toString(j)));
                        parent_layout.removeView(parent_layout.findViewWithTag("fill_" +Integer.toString(i)+"_"+Integer.toString(j)));
                    }
                }

                for (int i = 1; i <= number_of_nodes; i++)
                    parent_layout.removeView(parent_layout.findViewWithTag("node"+i));

                initialize_matrix();

                find_answer();

                draw_graph();

                findViewById(R.id.button_done).setAlpha(0.5f);
                matched=0;
            }
        }
    }

    private final class MyClickListener implements OnLongClickListener
    {
        @Override
        public boolean onLongClick(View view) {
            ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());
            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            v1=Integer.parseInt(view.getTag().toString().replaceAll("[^0-9]",""));
            current_y=view.getY();
            view.startDrag( data, //data to be dragged
                    shadowBuilder, //drag shadow
                    view, //local data about the drag and drop operation
                    0   //no needed flags
            );
            for(int i=1;i<=number_of_nodes;i++)
            {
                String tags="fill_"+v1+"_"+i;
                LinearLayout ll=parent_layout.findViewWithTag(tags);
                ll.setAlpha(0.5f);
                GradientDrawable gd=(GradientDrawable) ll.getBackground();
                gd.setColor(getResources().getColor(R.color.colorAccent));
                tags="fill_"+i+"_"+v1;
                parent_layout.findViewWithTag(tags).setAlpha(0.5f);
                ll=parent_layout.findViewWithTag(tags);
                ll.setAlpha(0.5f);
                gd=(GradientDrawable) ll.getBackground();
                gd.setColor(getResources().getColor(R.color.colorAccent));
            }
            view.setAlpha(0.5f);
            //view.setVisibility(View.GONE);
            return true;
        }
    }

    class MyDragListener implements OnDragListener
    {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v2=Integer.parseInt(v.getTag().toString().replaceAll("[^0-9]",""));
                    if(v2!=v1) {
                        float new_y;
                        new_y = v.getY();
                        rotate();
                        v.setY(current_y);
                        current_y = new_y;
                        View view = (View) event.getLocalState();
                        view.setY(current_y);
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    View view = (View) event.getLocalState();
                    for(int i=1;i<=number_of_nodes;i++)
                    {
                        String tags="fill_"+v1+"_"+i;
                        LinearLayout ll=parent_layout.findViewWithTag(tags);
                        ll.setAlpha(1.0f);
                        GradientDrawable gd=(GradientDrawable) ll.getBackground();
                        gd.setColor(Color.TRANSPARENT);
                        tags="fill_"+i+"_"+v1;
                        parent_layout.findViewWithTag(tags).setAlpha(1.0f);
                        ll=parent_layout.findViewWithTag(tags);
                        ll.setAlpha(1.0f);
                        gd=(GradientDrawable) ll.getBackground();
                        gd.setColor(Color.TRANSPARENT);
                    }
                    view.setAlpha(1.0f);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}
