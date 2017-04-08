package com.service;
 
import java.io.File;
import java.io.IOException;
 

import javax.sound.sampled.*;
import javax.swing.JSlider;
import javax.swing.JTable;

import com.list.MusicList;
import com.list.ThreadList;
import com.list.ViewList;
import com.model.Model;
import com.model.Music;
 

public class Player extends Thread{
	
	public Player(JSlider jSliderPlayProgress) {
		super();
		this.jSliderPlayProgress=jSliderPlayProgress;
	}

	private Player p;
	private long time = 0;

	Object lock = new Object();//一个空的对象

	private boolean paused = false;// 暂停 继续
	
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	private JSlider jSliderPlayProgress;//播放进度条
	private boolean over = false;//开始 结束
	
	 //是否自动播放下一曲
	private boolean isNext=true;
	
	AudioInputStream din = null;
	SourceDataLine line=null;
	
	/*private FloatControl volume = null;
	private JSlider jSliderVolume; 
	
	public JSlider getjSliderVolume() {
		return jSliderVolume;
	}

	public void setjSliderVolume(JSlider jSliderVolume) {
		this.jSliderVolume = jSliderVolume;
	
	}*/
	
	private Music music;
	
	public Music getMusic() {
		return music;
	}

	public void setMusic(Music music) {
		this.music = music;
	}
	
	 //播放音乐
	public  void run(){
		
		AudioInputStream in=null;
		
		try {
			
			File file = new File(music.getPath());
		
			//播放不了的歌曲，直接下一首
			try {
				 in = AudioSystem.getAudioInputStream(file);
			} catch (Exception e) {
				
				ViewList.getList().get(0).getJt().setModel(new Model());
				
				nextmusic();
			}
			
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			if(baseFormat.getEncoding()==AudioFormat.Encoding.PCM_UNSIGNED || baseFormat.getEncoding()==AudioFormat.Encoding.ULAW ||
					baseFormat.getEncoding()==AudioFormat.Encoding.ALAW || baseFormat.getEncoding()==AudioFormat.Encoding.PCM_SIGNED){
	             		time=(file.length()*8000000)/((int)(decodedFormat.getSampleRate()*baseFormat.getSampleSizeInBits()));
	            }else{
	                 int bitrate=0;
	                 if(baseFormat.properties().get("bitrate")!=null){
	                	//取得播放速度(单位位每秒)
	                     bitrate=(int)((Integer)(baseFormat.properties().get("bitrate")));
	                     if(bitrate!=0)
	                     time=(file.length()*8000000)/bitrate;
	                 }
	                 
	            }
			
			//line 打开文件
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open();
			//设置歌曲进度条的时间
			jSliderPlayProgress.setMaximum((int)time);
	    	jSliderPlayProgress.setValue(0);
	    	
			if(line!=null){
				line.open(decodedFormat);
				byte[] data = new byte[4096];
				int nBytesRead;
				
			synchronized (lock) {
				while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
					while (paused) {
						if(line.isRunning()) {
							line.stop();
							System.out.println("暂停");
						}
						try {
							lock.wait();
							System.out.println("等待");
						}
						catch(InterruptedException e) {
						}
					}
					if(!line.isRunning()&&!over) {
						System.out.println("开始播放");
						line.start();
					}
					
					if (over&&line.isRunning()) {
						System.out.println("停止播放");
						jSliderPlayProgress.setValue(0);
						isNext=false;
						line.drain();
						line.stop();
						line.close();
					}
					
					jSliderPlayProgress.setValue((int)line.getMicrosecondPosition());
					line.write(data, 0, nBytesRead);
				}
				
				nextmusic();
			}
			
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(din != null) {
				try {
					din.close(); 
				} catch(IOException e) {
					
				}
			}
		}
	}

	private void nextmusic() {
		int currentid=Integer.parseInt(this.music.getId());
		int nextid=0;//将要播放的id
		if (isNext&&!(currentid==MusicList.getList().size()-1)){
			nextid=currentid+1;
			JTable jTable=ViewList.getList().get(0).getJt();
			if(nextid==0){//第一个			
				jTable.setRowSelectionInterval(0,0);
				
			}else {
					jTable.setRowSelectionInterval(nextid-1,nextid); 
			}
				  this.stopplay();
				  ThreadList.getList().clear();
				  p=new Player(jSliderPlayProgress);
				  p.setMusic(MusicList.getList().get(nextid));
				  ThreadList.getList().add(p);
				  p.start();
		}
			
	}
	
	//开始
	public void startplay(){
		over=false;
	}
	
	//停止
	
	public void stopplay(){
		over=true;
	}
	
	
	// 暂停
	public void userPressedPause() {
	 paused = true;
	}
	 
	//继续
	public void userPressedPlay() {
			 synchronized(lock) {
				  paused = false;
				  lock.notifyAll();
		}

	}
	
	public void Pause(){
		if (paused) {
			//保证在同一时刻最多只有一个线程执行
			 synchronized(lock) {
				  paused = false;
				  lock.notifyAll();
				 }
		}else{
			 paused = true;
		}
	}
}