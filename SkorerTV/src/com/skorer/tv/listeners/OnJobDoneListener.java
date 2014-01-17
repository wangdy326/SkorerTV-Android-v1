package com.skorer.tv.listeners;

import android.os.AsyncTask;

/**
 * Simple interface far adding dynamic callback.
 * 
 * @author Gökhan Barış Aker (gokhanbarisaker@gmail.com | gokhan@mobilike.com)
 */
abstract public class OnJobDoneListener<E>
{
	public class JobStatus
    {
		public static final int SUCCEED = 1;
		public static final int FAILED = 0;
		public static final int CANCELLED = -1;
    }
	
	public abstract void onJobDone(int status, E result);
	
	/**
	 * 
	 * @param status {@code OnJobDoneListener.JobStatus}
	 */
	public void completeJobAsynch(int status)
	{
		this.completeJobAsynch(status, null);
	}
	
	/**
	 * 
	 * @param status {@code OnJobDoneListener.JobStatus}
	 * @param attachment Attachment object for flexible job callback. This parameter is optional
	 */
	public void completeJobAsynch(int status, E attachment)
	{
		new JobStatusCallbackWorker()
		.setJobStatus(status)
		.setAttachment(attachment)
		.execute(this, null, null);
	}
	
	private class JobStatusCallbackWorker extends AsyncTask<OnJobDoneListener<E>, Void, Void>
	{
		private int jobStatus = JobStatus.FAILED;
		private E attachment = null;

		@Override
		protected Void doInBackground(OnJobDoneListener<E>... params)
		{
			// Initialize listener as null (invalid)
			OnJobDoneListener<E> listener = null;
						
			// If parameters provided
			if((params != null) && (params.length > 0))
			{
				// Get listener reference
				listener = params[0];
			}
			
			listener.onJobDone(jobStatus, attachment);
			
			return null;
		}
		
		JobStatusCallbackWorker setJobStatus(int status)
		{
			this.jobStatus = status;
			
			return this;
		}
		
		JobStatusCallbackWorker setAttachment(E attachment)
		{
			this.attachment = attachment;
			
			return this;
		}
	}
}
