package disciplina.lip.AuxiliarRotinaEstudo.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstatisticaDisciplinaDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstudoDiarioDTO;
import disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.RevisaoStatusDTO;
import disciplina.lip.AuxiliarRotinaEstudo.model.entity.Usuario;
import disciplina.lip.AuxiliarRotinaEstudo.repository.RelatorioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
@Transactional(readOnly = true)
public class RelatorioRepositoryImpl implements RelatorioRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<EstatisticaDisciplinaDTO> findEstatisticasPorDisciplina(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT e.nomeDisciplina, " +
                     "   COUNT(e), " +
                     "   SUM(e.tempoDeEstudo), " +
                     "   AVG(e.tempoDeEstudo) " +
                     "FROM Estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND e.diaDoEstudo BETWEEN :inicio AND :fim " +
                     "GROUP BY e.nomeDisciplina " +
                     "ORDER BY COUNT(e) DESC";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        List<Object[]> results = query.getResultList();
        
        return results.stream().map(result -> {
            String disciplina = (String) result[0];
            Long count = (Long) result[1];
            Long tempoTotal = (Long) result[2];
            Double mediaTempo = (Double) result[3];
            
            Long revisoesConcluidas = countRevisoesConcluidasPorDisciplina(usuario, disciplina, inicio, fim);
            
            return new EstatisticaDisciplinaDTO(
                disciplina,
                count.intValue(),
                tempoTotal != null ? tempoTotal.intValue() : 0,
                mediaTempo != null ? mediaTempo : 0.0,
                revisoesConcluidas != null ? revisoesConcluidas.intValue() : 0
            );
        }).collect(Collectors.toList());
    }
    
    private Long countRevisoesConcluidasPorDisciplina(Usuario usuario, String disciplina, LocalDate inicio, LocalDate fim) {
        try {
            String jpql = "SELECT COUNT(r) FROM Revisao r " +
                         "JOIN r.estudo e " +
                         "WHERE e.usuario = :usuario " +
                         "AND e.nomeDisciplina = :disciplina " +
                         "AND r.concluida = true " +
                         "AND r.dataRevisao BETWEEN :inicio AND :fim";
            
            TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
            query.setParameter("usuario", usuario);
            query.setParameter("disciplina", disciplina);
            query.setParameter("inicio", inicio);
            query.setParameter("fim", fim);
            
            return query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public List<EstudoDiarioDTO> findEstudosPorDia(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT new disciplina.lip.AuxiliarRotinaEstudo.dto.relatorio.EstudoDiarioDTO( " +
                     "   e.diaDoEstudo, " +
                     "   COUNT(e), " +
                     "   COALESCE(SUM(e.tempoDeEstudo), 0) " +
                     ") " +
                     "FROM Estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND e.diaDoEstudo BETWEEN :inicio AND :fim " +
                     "GROUP BY e.diaDoEstudo " +
                     "ORDER BY e.diaDoEstudo";
        
        TypedQuery<EstudoDiarioDTO> query = entityManager.createQuery(jpql, EstudoDiarioDTO.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        return query.getResultList();
    }
    
    @Override
    public long countEstudosPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT COUNT(e) FROM Estudo e WHERE e.usuario = :usuario AND e.diaDoEstudo BETWEEN :inicio AND :fim";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public long countRevisoesConcluidasPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT COUNT(r) FROM Revisao r " +
                     "JOIN r.estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND r.concluida = true " +
                     "AND r.dataRevisao BETWEEN :inicio AND :fim";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public long countRevisoesPendentesPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT COUNT(r) FROM Revisao r " +
                     "JOIN r.estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND r.concluida = false " +
                     "AND r.dataRevisao BETWEEN :inicio AND :fim " +
                     "AND r.dataRevisao = CURRENT_DATE";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public long countRevisoesAtrasadasPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT COUNT(r) FROM Revisao r " +
                     "JOIN r.estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND r.concluida = false " +
                     "AND r.dataRevisao BETWEEN :inicio AND :fim " +
                     "AND r.dataRevisao < CURRENT_DATE";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    @Override
    public Double findMediaTempoDiarioPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT AVG(e.tempoDeEstudo) FROM Estudo e WHERE e.usuario = :usuario AND e.diaDoEstudo BETWEEN :inicio AND :fim";
        TypedQuery<Double> query = entityManager.createQuery(jpql, Double.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    @Override
    public List<String> findDisciplinaMaisEstudadaPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT e.nomeDisciplina FROM Estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND e.diaDoEstudo BETWEEN :inicio AND :fim " +
                     "GROUP BY e.nomeDisciplina " +
                     "ORDER BY COUNT(e) DESC";
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        query.setMaxResults(1);
        
        try {
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    @Override
    public Integer findTempoTotalPeriodo(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT COALESCE(SUM(e.tempoDeEstudo), 0) FROM Estudo e WHERE e.usuario = :usuario AND e.diaDoEstudo BETWEEN :inicio AND :fim";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            Long result = query.getSingleResult();
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public List<Object[]> findTempoPorDisciplina(Usuario usuario, LocalDate inicio, LocalDate fim) {
        String jpql = "SELECT e.nomeDisciplina, COALESCE(SUM(e.tempoDeEstudo), 0) " +
                     "FROM Estudo e " +
                     "WHERE e.usuario = :usuario " +
                     "AND e.diaDoEstudo BETWEEN :inicio AND :fim " +
                     "GROUP BY e.nomeDisciplina";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("usuario", usuario);
        query.setParameter("inicio", inicio);
        query.setParameter("fim", fim);
        
        try {
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    @Override
    public List<RevisaoStatusDTO> findStatusRevisoes(Usuario usuario) {
        try {
            String jpqlConcluidas = "SELECT COUNT(r) FROM Revisao r " +
                                   "JOIN r.estudo e " +
                                   "WHERE e.usuario = :usuario " +
                                   "AND r.concluida = true";
            
            String jpqlPendentes = "SELECT COUNT(r) FROM Revisao r " +
                                  "JOIN r.estudo e " +
                                  "WHERE e.usuario = :usuario " +
                                  "AND r.concluida = false " +
                                  "AND r.dataRevisao = CURRENT_DATE";
            
            String jpqlAtrasadas = "SELECT COUNT(r) FROM Revisao r " +
                                  "JOIN r.estudo e " +
                                  "WHERE e.usuario = :usuario " +
                                  "AND r.concluida = false " +
                                  "AND r.dataRevisao < CURRENT_DATE";
            
            TypedQuery<Long> queryConcluidas = entityManager.createQuery(jpqlConcluidas, Long.class);
            queryConcluidas.setParameter("usuario", usuario);
            
            TypedQuery<Long> queryPendentes = entityManager.createQuery(jpqlPendentes, Long.class);
            queryPendentes.setParameter("usuario", usuario);
            
            TypedQuery<Long> queryAtrasadas = entityManager.createQuery(jpqlAtrasadas, Long.class);
            queryAtrasadas.setParameter("usuario", usuario);
            
            long concluidas = queryConcluidas.getSingleResult();
            long pendentes = queryPendentes.getSingleResult();
            long atrasadas = queryAtrasadas.getSingleResult();
            
            return List.of(
                new RevisaoStatusDTO("Concluídas", concluidas),
                new RevisaoStatusDTO("Pendentes", pendentes),
                new RevisaoStatusDTO("Atrasadas", atrasadas)
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(
                new RevisaoStatusDTO("Concluídas", 0),
                new RevisaoStatusDTO("Pendentes", 0),
                new RevisaoStatusDTO("Atrasadas", 0)
            );
        }
    }
}