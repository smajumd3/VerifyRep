package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.MapFile;

@Repository
public class MapFileDaoImpl implements MapFileDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapFile> getMapFileList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<MapFile> mapFileList = session.createQuery("from MapFile").list();
		return mapFileList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapFile> getMapFileListByOeration(Long operationId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<MapFile> query = session.createQuery("from MapFile mapFile where mapFile.operationId=?1");
		query.setParameter(1, operationId);
		
		List<MapFile> mapFiles;
		try {
			mapFiles = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return mapFiles;
	}

	@Override
	public MapFile getMapFile(Long mapFileId) {
		Session session = this.sessionFactory.getCurrentSession();
		MapFile mapFile = (MapFile) session.get(MapFile.class, mapFileId);
		return mapFile;
	}

	@Override
	public void addMapFile(MapFile mapFile) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(mapFile);
		session.flush();
	}

	@Override
	public void updateMapFile(MapFile mapFile) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(mapFile);
		session.flush();
	}

	@Override
	public void deleteMapFile(Long mapFileId) {
		Session session = this.sessionFactory.getCurrentSession();
		MapFile mapFile = (MapFile) session.load(MapFile.class, mapFileId);
		if (null != mapFile) {
			session.delete(mapFile);
		}	
	}

}
