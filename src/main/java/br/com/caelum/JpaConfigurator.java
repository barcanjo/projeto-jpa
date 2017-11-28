package br.com.caelum;

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@EnableTransactionManagement
public class JpaConfigurator {

	/**
	 * Define o Bean utilizada para fornecer um DataSource de conexao com o
	 * banco de dados. O atributo destroyMethod na anotação @Bean define o
	 * método (close) do Pool que o Spring chama quando o Tomcat é desligado.
	 * Assim garantimos que todas as conexões serão fechadas corretamente.
	 *
	 * @return Uma instancia de DataSource pronta para ser utilizada na conexao
	 *         com o banco de dados da aplicacao.
	 *
	 * @throws PropertyVetoException
	 */
	@Bean(destroyMethod = "close")
	public DataSource getDataSource() throws PropertyVetoException {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();

		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl("jdbc:mysql://localhost/projeto_jpa");
		dataSource.setUser("root");
		dataSource.setPassword("www.");

		dataSource.setMinPoolSize(3);
		dataSource.setMaxPoolSize(5);
		dataSource.setIdleConnectionTestPeriod(1); // a cada um segundo testamos
													// as conexões ociosas

		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean getEntityManagerFactory(final DataSource dataSource) {
		final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setPackagesToScan("br.com.caelum");
		entityManagerFactory.setDataSource(dataSource);

		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		final Properties props = new Properties();

		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		props.setProperty("hibernate.show_sql", "true");
		props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		props.setProperty("hibernate.cache.use_second_level_cache", "true"); // habilita
																				// o
																				// uso
																				// do
																				// cache
																				// de
																				// segundo
																				// nivel
		props.setProperty("hibernate.cache.region.factory_class",
				"org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory"); // habilita
																				// o
																				// EhCache
																				// como
																				// provedor
																				// do
																				// cache
																				// de
																				// segundo
																				// nivel
		props.setProperty("hibernate.cache.use_query_cache", "true"); // habilita
																		// o uso
																		// de
																		// cache
																		// em
																		// query

		props.setProperty("hibernate.generate_statistics", "true"); // ativa o
																	// mecanismo
																	// de
																	// estatisticas
																	// do
																	// Hibernate
		entityManagerFactory.setJpaProperties(props);
		return entityManagerFactory;
	}

	@Bean
	public JpaTransactionManager getTransactionManager(final EntityManagerFactory emf) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);

		return transactionManager;
	}

	/**
	 * Cria um Bean da interface Statistics que sera utilizada para coletar
	 * dados do Hibernate referente a conexoes, cache etc.
	 *
	 * @param emf
	 *            A instancia do EntityManagerFactory da aplicacao que sera
	 *            injetada pelo contexto do Spring.
	 * @return Uma instancia valida de Statistics criada a partir da sessao
	 *         criada pelo emf.
	 */
	@Bean
	public Statistics statistics(EntityManagerFactory emf) {
		return emf.unwrap(SessionFactory.class).getStatistics();
	}
}
