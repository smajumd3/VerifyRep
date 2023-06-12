package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.BuildRuleFile;

@Repository
public class BuildRuleFileDaoImpl implements BuildRuleFileDao {

	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<BuildRuleFile> getBuildFileList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<BuildRuleFile> fileList = session.createQuery("from BuildRuleFile").list();
		return fileList;
	}

	@Override
	public BuildRuleFile getBuildFileById(Long fileId) {
		Session session = this.sessionFactory.getCurrentSession();
        BuildRuleFile file = (BuildRuleFile) session.get(BuildRuleFile.class, fileId);
		return file;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BuildRuleFile> getBuildFilesByUser(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<BuildRuleFile> query = session.createQuery("from BuildRuleFile file where file.userId=?1");
		query.setParameter(1, userId);
		
		List<BuildRuleFile> files;
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
	public List<BuildRuleFile> getBuildFilesByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<BuildRuleFile> query = session.createQuery("from BuildRuleFile file where file.client=?1");
		query.setParameter(1, client);
		
		List<BuildRuleFile> files;
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
	public List<BuildRuleFile> getBuildFilesByFileNameClient(String fileName, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<BuildRuleFile> query = session.createQuery("from BuildRuleFile file where file.fileName=?1 and file.client=?2");
		query.setParameter(1, fileName);
		query.setParameter(2, client);
		
		List<BuildRuleFile> files;
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
	public BuildRuleFile getBuildFileByFileNameClient(String fileName, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<BuildRuleFile> query = session.createQuery("from BuildRuleFile file where file.fileName=?1 and file.client=?2");
		query.setParameter(1, fileName);
		query.setParameter(2, client);
		
		BuildRuleFile file;
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
	public BuildRuleFile getBuildFileByFileName(String fileName) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<BuildRuleFile> query = session.createQuery("from BuildRuleFile file where file.fileName=?1");
		query.setParameter(1, fileName);
		
		BuildRuleFile file;
		try {
			file = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return file;
	}

	@Override
	public void addBuildFile(BuildRuleFile file) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(file);
		session.flush();
	}

	@Override
	public void updateBuildFile(BuildRuleFile file) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(file);
		session.flush();
	}

	@Override
	public void deleteBuildFile(Long fileId) {
		Session session = this.sessionFactory.getCurrentSession();
		BuildRuleFile file = (BuildRuleFile) session.load(BuildRuleFile.class, fileId);
		if (null != file) {
			session.delete(file);
		}
	}

}
