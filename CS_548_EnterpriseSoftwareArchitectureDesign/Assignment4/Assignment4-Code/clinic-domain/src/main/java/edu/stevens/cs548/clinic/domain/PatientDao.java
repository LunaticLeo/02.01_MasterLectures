package edu.stevens.cs548.clinic.domain;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import edu.stevens.cs548.clinic.domain.ClinicDomainProducer.ClinicDomain;

// TODO
@RequestScoped
public class PatientDao implements IPatientDao {

	// TODO
	@Inject 
	@ClinicDomain
	private EntityManager em;
	
	// TODO
	@Inject
	private ITreatmentDao treatmentDao;


	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(PatientDao.class.getCanonicalName());

	@Override
	public void addPatient(Patient patient) throws PatientExn {
		UUID pid = patient.getPatientId();
		Query query = em.createNamedQuery("CountPatientByPatientId").setParameter("patientId", pid);
		Long numExisting = (Long) query.getSingleResult();
		
		if (numExisting < 1) {
			
			// TODO add to database, and initialize the patient aggregate with a treatment DAO.
			patient.setTreatmentDao(treatmentDao);
			em.persist(patient);
			
		} else {
			
			throw new PatientExn("Insertion: Patient with patient id (" + pid + ") already exists.");
		}
	}

	@Override
	public Patient getPatient(UUID id) throws PatientExn {
		/*
		 * Retrieve patient using external key
		 */
		TypedQuery<Patient> query = em.createNamedQuery("SearchPatientByPatientId", Patient.class).setParameter("patientId",id);
		List<Patient> patients = query.getResultList();
		
		if (patients.size() > 1) {
			throw new PatientExn("Duplicate patient records: patient id = " + id);
		} else if (patients.size() < 1) {
			throw new PatientExn("Patient not found: patient id = " + id);
		} else {
			Patient p = patients.get(0);
			p.setTreatmentDao(this.treatmentDao);
			return p;
		}
	}

	@Override
	public List<Patient> getPatients() {
		/*
		 * Return a list of all patients (remember to set treatmentDAO)
		 */
		TypedQuery<Patient> query = em.createNamedQuery("SearchAllPatients", Patient.class);
		List<Patient> patients = query.getResultList();
		
		for (Patient p : patients) {
			p.setTreatmentDao(treatmentDao);
		}

		return patients;
	}
	
	@Override
	public void deletePatients() {
		Query update = em.createNamedQuery("RemoveAllPatients");
		update.executeUpdate();
	}

}
