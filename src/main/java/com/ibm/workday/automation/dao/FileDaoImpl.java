package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.File;

@Repository
public class FileDaoImpl implements FileDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<File> getFileList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<File> fileList = session.createQuery("from File").list();
		return fileList;
	}

	@Override
	public File getFileById(Long fileId) {
		Session session = this.sessionFactory.getCurrentSession();
		File file = (File) session.get(File.class, fileId);
		return file;
	}

	@Override
	public void addFile(File file) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(file);
		session.flush();
	}
	
	@Override
	public void updateFile(File file) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(file);
		session.flush();
	}	

	@Override
	public void deleteFile(Long fileId) {
		Session session = this.sessionFactory.getCurrentSession();
		File file = (File) session.load(File.class, fileId);
		if (null != file) {
			session.delete(file);
		}	
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<File> getFilesByUserId(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.userId=?1");
		query.setParameter(1, userId);
		
		List<File> files;
		try {
			files = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return files;
	}

	@SuppressWarnings("unchecked")
	@Override
	public File getFileByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.client=?1");
		query.setParameter(1, client);
		
		File file;
		try {
			file = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return file;
	}

	@SuppressWarnings("unchecked")
	@Override
	public File getFileByFileNameClient(String fileName, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.fileName=?1 and file.client=?2");
		query.setParameter(1, fileName);
		query.setParameter(2, client);
		
		File file;
		try {
			file = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return file;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public File getFileByFileNameClientUser(String fileName, String client, Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.fileName=?1 and file.client=?2 and file.userId=?3");
		query.setParameter(1, fileName);
		query.setParameter(2, client);
		query.setParameter(3, userId);
		
		File file;
		try {
			file = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return file;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public List<File> getFilesByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.client=?1");
		query.setParameter(1, client);
		
		List<File> files;
		try {
			files = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return files;
	}

	@SuppressWarnings("unchecked")
	@Override
	public File getFileByFileName(String fileName) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.fileName=?1");
		query.setParameter(1, fileName);
		
		File file;
		try {
			file = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return file;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<File> getFilesByFileNameClient(String fileName, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<File> query = session.createQuery("from File file where file.fileName=?1 and file.client=?2");
		query.setParameter(1, fileName);
		query.setParameter(2, client);
		
		List<File> files;
		try {
			files = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return files;
	}

}
