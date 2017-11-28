package br.com.caelum.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import br.com.caelum.model.Loja;
import br.com.caelum.model.Produto;

@Repository
public class ProdutoDao {

	@PersistenceContext
	private EntityManager em;

	public Produto getProduto(Integer id) {
		Produto produto = em.find(Produto.class, id);
		return produto;
	}

	/**
	 * Consulta por um Produto e ao encontra-lo trava o registro para impedir
	 * alteracoes atraves de outra sessao.
	 *
	 * @param id
	 * @return
	 */
	public Produto getProdutoComLock(Integer id) {
		// trava o registro do banco para impedir alteracoes por outra sessao
		Produto produto = em.find(Produto.class, id, LockModeType.PESSIMISTIC_WRITE);
		// em.lock(produto, LockModeType.PESSIMISTIC_WRITE);
		return produto;
	}

	public List<Produto> getProdutos() {
		return em.createQuery("from Produto", Produto.class).getResultList();
	}

	/**
	 * Consulta por todos os produtos utilizando o grafo definido na classe
	 * Produto. Permite que seja feito um fetch na propriedade definida no
	 * grafo.
	 *
	 * @return Uma lista de produtos encontrados a partir da consulta.
	 */
	public List<Produto> getProdutosComEntityGraph() {
		TypedQuery<Produto> typedQuery = em.createQuery("select distinct p from Produto p", Produto.class);

		typedQuery.setHint("javax.persistence.loadgraph", em.createEntityGraph("produtoComCategoria"));
		typedQuery.setHint("org.hibernate.cacheable", "true");

		return typedQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Produto> getProdutosComHibernate(String nome, Integer categoriaId, Integer lojaId) {
		Session session = em.unwrap(Session.class);
		Criteria criteria = session.createCriteria(Produto.class);

		if (!nome.isEmpty()) {
			criteria.add(Restrictions.like("nome", "%" + nome + "%"));
		}

		if (null != lojaId) {
			criteria.add(Restrictions.like("loja.id", lojaId));
		}

		if (null != categoriaId) {
			criteria.setFetchMode("categorias", FetchMode.JOIN).createAlias("categorias", "c")
					.add(Restrictions.like("c.id", categoriaId));
		}
		return criteria.list();
	}

	public List<Produto> getProdutosComJPa(String nome, Integer categoriaId, Integer lojaId) {
		/*
		 * Cria um CriteriaBuilder a partir do EntityManager para que ele
		 * auxilie na construcao da query.
		 */
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		// A partir do CriteriaBuilder cria uma query, responsável por realizar
		// a consulta
		CriteriaQuery<Produto> query = criteriaBuilder.createQuery(Produto.class);

		// Atraves do Root pode-se chegar aos atributos da Classe da query
		Root<Produto> root = query.from(Produto.class);

		// retorno o Path do atributo nome da classe da query
		Path<String> nomePath = root.<String>get("nome");

		// retorno o Path do atributo id, do atributo loja da classe da query
		Path<Integer> lojaPath = root.<Loja>get("loja").<Integer>get("id");

		/*
		 * Retorno o path do atributo id da classe da query, mas antes faz um
		 * join ja que existe um relacionamento @ManyToMany
		 */
		Path<Integer> categoriaPath = root.join("categorias").<Integer>get("id");

		List<Predicate> predicates = new ArrayList<Predicate>();

		if (!nome.isEmpty()) {
			Predicate nomeIgual = criteriaBuilder.like(nomePath, "%" + nome + "%");
			predicates.add(nomeIgual);
		}

		if (null != lojaId) {
			Predicate lojaIgual = criteriaBuilder.equal(lojaPath, lojaId);
			predicates.add(lojaIgual);
		}

		if (null != categoriaId) {
			Predicate categoriaIgual = criteriaBuilder.equal(categoriaPath, categoriaId);
			predicates.add(categoriaIgual);
		}

		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Produto> typedQuery = em.createQuery(query);
		typedQuery.setHint("org.hibernate.cacheable", "true"); // avisa ao
																// Hibernate
																// para salvar
																// no cache o
																// resultado da
																// query

		return typedQuery.getResultList();
	}

	public void insere(Produto produto) {
		if (produto.getId() == null) {
			em.persist(produto);
		} else {
			em.merge(produto);
		}
	}

}
