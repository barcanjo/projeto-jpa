package br.com.caelum.teste;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import br.com.caelum.JpaConfigurator;
import br.com.caelum.model.Produto;

public class TesteLockPessimista {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JpaConfigurator.class);
		EntityManagerFactory factory = context.getBean(EntityManagerFactory.class);

		EntityManager em1 = factory.createEntityManager();
		EntityManager em2 = factory.createEntityManager();

		em1.getTransaction().begin();
		em2.getTransaction().begin();

		Produto produto1Em1 = em1.find(Produto.class, 1, LockModeType.PESSIMISTIC_WRITE);
		produto1Em1.setNome("Estude com os melhores professores e aprenda no seu ritmo, sem sair de casa.");

		em2.find(Produto.class, 1, LockModeType.PESSIMISTIC_WRITE);

		em1.getTransaction().commit();
		em2.getTransaction().commit();

		em1.close();
		em2.close();
		context.close();
	}

}
