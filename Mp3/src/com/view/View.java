package com.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;



import com.list.MusicList;
import com.list.ThreadList;
import com.list.ViewList;
import com.model.Model;
import com.model.Music;
import com.service.Player;
//import com.util.DirInput;
import com.util.FileInput;
import com.util.FileList;
//import com.util.List_File;


public class View extends JFrame implements MouseListener,ActionListener,WindowListener {
	
	private JButton stop, open,next,pre;
	private  Player p;
	private JPanel[] jPanels;
	private MusicList list;
	private JScrollPane jsp;
	private JTable jt;
	private JRootPane j;
	private Model model;
	private JSlider jSliderPlayProgress;
	private FileInput fileinput;
    private JMenuBar jb;
    private JMenuItem fm;
    
	public View(){
		System.out.println(ViewList.getList().size());
		if (ViewList.getList().size()==0) {
			Open();
		}
	}
	
	private void Open() {
		
       //菜单选项
		jb=new JMenuBar();
		fm=new JMenuItem("文件");
		fm.addActionListener(this);
		
		jb.add(fm);

		JPanel p1=new JPanel();
		JPanel p2=new JPanel();
		JPanel p3=new JPanel();
		
		//增加菜单选项
		j=new JRootPane();
		j.setJMenuBar(jb);
		p2.add(j);
		
		 open=new JButton("播放");
		 stop=new JButton("停止");
		 open.addMouseListener(this);
		 stop.addMouseListener(this);
		 pre=new JButton("上一首");
		 next=new JButton("下一首");
		 pre.addMouseListener(this);
		 next.addMouseListener(this);
		 //网格布局，2行1列
		p1.setLayout(new GridLayout(2,1));
		
		JPanel jPanel2=new JPanel();
		jPanel2.add(open);
		jPanel2.add(stop);
		jPanel2.add(pre);
		jPanel2.add(next);
		
		p1.add(jPanel2);
		
		//播放进度条
		 jSliderPlayProgress = new JSlider();   
	     jSliderPlayProgress.setValue(0);
	     jSliderPlayProgress.setEnabled(false);
         jSliderPlayProgress.setPreferredSize(new Dimension(200, 20));
	     p1.add(jSliderPlayProgress);
	    
		
		jPanels=new JPanel[list.getList().size()];
		
		for (int i = 0; i < list.getList().size(); i++) {
			Music music=list.getList().get(i);
			JPanel jPanel=new MyJPanel(music);
			JLabel jLabel=new JLabel(music.getName(),SwingConstants.CENTER);
			jLabel.setSize(300, 10);
			jLabel.setHorizontalTextPosition(JLabel.CENTER);
			jPanel.setBackground(Color.WHITE);
			
			jPanels[i]=jPanel;
			jPanel.addMouseListener(this);
			jPanel.add(jLabel);
			p3.add(jPanel);
		}
		
		p3.setBackground(Color.WHITE);
	    p3.setLayout(new GridLayout(10, 1));
		p3.setSize(320, 500);
		
		this.add(p1,BorderLayout.NORTH);
		this.add(p2,BorderLayout.SOUTH);
		
		//添加表
		model=new Model();			
		
		jt=new JTable(model){ // 设置jtable的单元格为透明的
			   public Component prepareRenderer(TableCellRenderer renderer,
					     int row, int column) {
					    Component c = super.prepareRenderer(renderer, row, column);
					    if (c instanceof JComponent) {
					     ((JComponent) c).setOpaque(false);
					    }
					    return c;
					   }
					  };;
					  
		jt.setOpaque(false);
		
		jt.setRowHeight(30);
		jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		jt.setShowHorizontalLines(false);
		jt.setSelectionBackground(new Color(189,215,238));
		jt.addMouseListener(this);
		
		jsp = new JScrollPane(jt);
		
		jsp.setOpaque(false);
		jsp.getViewport().setOpaque(false);
		
		this.add(jsp,BorderLayout.CENTER);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addWindowListener(this);
		
		this.getContentPane().add(jsp); 
		this.setLocation(400, 200);
		this.setSize(337, 525);
		this.setResizable(false);
		this.setVisible(true);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		System.out.println("开始播放");
		
		if (e.getSource()==open) {
			
			if (p==null) {//开始
			  p=new Player(jSliderPlayProgress);
			  p.setMusic(MusicList.getList().get(0));
			  jt.setRowSelectionInterval(0,0);
			  ThreadList.add(p);
			  open.setText("暂停");
			  p.start();
			}else{//继续
				if (ThreadList.getList().size()!=0) {
					p=ThreadList.getList().get(0);
				}
				
				String s=p.isPaused()?"暂停":"播放";
				open.setText(s);
				p.Pause();
			}
			
		}else if (e.getSource()==stop) {
			if (ThreadList.getList().size()!=0) {
				p=ThreadList.getList().get(0);
			}
			if (p!=null) {
				p.stopplay();
				p=null;
				open.setText("播放");
			}
		
			
		}else if (e.getSource()==pre) {//上一首
			premusic();

		}else if (e.getSource()==next) {//下一首
			nextmusic();
		}else if (e.getSource()==jt&&e.getClickCount()==2) {//双击
			clickmusic();
		}
	}

	private void clickmusic() {
		//双击Jtable
		System.out.println("点击了");
		
		int rowNum = this.jt.getSelectedRow();
//		System.out.println(rowNum);
		if(rowNum == -1) {
			JOptionPane.showMessageDialog(this, "你没有选择一项");
			return;
		}
		
		ArrayList<Player> list=ThreadList.getList();
//		System.out.println(list.size()+"音乐文件数目");
		
		 if (list.size()==0) {
			 
			 p=new Player(jSliderPlayProgress);
			  p.setMusic(MusicList.getList().get(rowNum));
			  ThreadList.add(p);
			  open.setText("暂停");
			  p.start();
		}else{
//			System.out.println("停止");
		list.get(0).stopplay();
		list.clear();
		p=new Player(jSliderPlayProgress);
		    p.setMusic(MusicList.getList().get(rowNum));
		    open.setText("暂停");
		    list.add(p);
		    p.start();
		}
	}


	public JTable getJt() {
		return jt;
	}

	private void premusic() {
		System.out.println("上一首");
		
		ArrayList<Player> list=ThreadList.getList();
		
		int id=Integer.parseInt(list.get(0).getMusic().getId());
		
		if(id!=0){
		if (id==1) {
			jt.setRowSelectionInterval(0,0);
		}else{
			jt.setRowSelectionInterval(id-2,id-1);
		}
		System.out.println(id);
		
		list.get(0).stopplay();
		list.clear();
		
		  p=new Player(jSliderPlayProgress);

		  p.setMusic(MusicList.getList().get(id-1));
		  System.out.println(id-1);
		  
		  open.setText("暂停");
		  list.add(p);
		  p.start();
		}
	}

	private void nextmusic() {
		System.out.println("下一首");
		ArrayList<Player> list=ThreadList.getList();
		int id=Integer.parseInt(list.get(0).getMusic().getId());
		
//		System.out.println(id);
		if(id!=MusicList.getList().size()-1){ 
			
		jt.setRowSelectionInterval(id,id+1); 
		
		list.get(0).stopplay();
		list.clear();
		
		p=new Player(jSliderPlayProgress);
		
		  p.setMusic(MusicList.getList().get(id+1));
//		  System.out.println(id+1);
		  
		  open.setText("暂停");
		  list.add(p);
		  p.start();
}
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==fm) {//添加mp3文件
		if(fileinput==null)	fileinput = new FileInput(this);
        	fileinput.open();
        	File[] s=fileinput.getFiles();
        	ArrayList<Music> musiclist=MusicList.getList();
        	
        	if(s!=null){
        	    for(int i=0;i<s.length;i++){
        	    Music music= new Music();
	        	music.setId(musiclist.size()+"");
	        	music.setName(s[i].getName());
	        	music.setPath(s[i].getAbsolutePath());
        	    musiclist.add(music);
        	    jt.setModel(new Model());
        	    }
        	}
		}
	}



	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		System.out.println("关闭kk");
	}


	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.out.println("close");
		
		if (MusicList.getList().size()!=0) {
			System.out.println("写入文件");
			//清空之前的内容
			FileList.clear("file/filelist.txt");
			ArrayList<Music> list=MusicList.getList();
			for (int i = 0; i < list.size(); i++) {
				FileList.writeFile("file/filelist.txt",list.get(i).getId()+","+list.get(i).getName()+","
						+list.get(i).getPath()+"\n");
			}
		}
	}


	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		System.out.println("open");
		
		File file=new File("file/filelist.txt");
		
		if (file.exists()==false) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else {

			FileList.readFileByLines("file/filelist.txt");
			jt.setModel(new Model());
		}
	}
}
