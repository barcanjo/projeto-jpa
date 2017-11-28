package br.com.caelum;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import br.com.caelum.dao.CategoriaDao;
import br.com.caelum.dao.LojaDao;
import br.com.caelum.dao.ProdutoDao;
import br.com.caelum.model.Categoria;
import br.com.caelum.model.Loja;
import br.com.caelum.model.Produto;

@Configuration
@EnableWebMvc
@ComponentScan("br.com.caelum")
@EnableTransactionManagement
public class Configurador extends WebMvcConfigurerAdapter {

	@Override
	public void addFormatters(final FormatterRegistry registry) {
		registry.addConverter(new Converter<String, Categoria>() {

			@Override
			public Categoria convert(final String categoriaId) {
				final Categoria categoria = new Categoria();
				categoria.setId(Integer.valueOf(categoriaId));

				return categoria;
			}

		});
	}

	/**
	 * Adiciona interceptadores ao projeto.
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addWebRequestInterceptor(getOpenEntityManagerInViewInterceptor());
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/").setCachePeriod(60)
				.resourceChain(true).addResolver(new PathResourceResolver());
	}

	@Bean
	public List<Categoria> categorias(final CategoriaDao categoriaDao) {
		final List<Categoria> categorias = categoriaDao.getCategorias();

		return categorias;
	}

	/**
	 * Cria uma instancia OpenEntityManagerInViewInterceptor, um interceptador
	 * (filtro) que permite abrir uma conexao (EntityManager) e fechar apenas
	 * quando todos os dados da view forem exibidos, evitando o
	 * LazyInitializationException.
	 *
	 * @return Uma instancia valida de OpenEntityManagerInViewInterceptor.
	 */
	@Bean
	public OpenEntityManagerInViewInterceptor getOpenEntityManagerInViewInterceptor() {
		return new OpenEntityManagerInViewInterceptor();
	}

	@Bean
	public ViewResolver getViewResolver() {
		final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".jsp");

		viewResolver.setExposeContextBeansAsAttributes(true);

		return viewResolver;
	}

	@Bean
	public List<Loja> lojas(final LojaDao lojaDao) {
		final List<Loja> lojas = lojaDao.getLojas();

		return lojas;
	}

	@Bean
	public MessageSource messageSource() {
		final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

		messageSource.setBasename("/WEB-INF/messages");
		messageSource.setCacheSeconds(1);
		messageSource.setDefaultEncoding("ISO-8859-1");

		return messageSource;

	}

	@Bean
	@Scope("request")
	public List<Produto> produtos(final ProdutoDao produtoDao) {
		final List<Produto> produtos = produtoDao.getProdutosComEntityGraph();

		return produtos;
	}
}
