package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.http.protocol.HTTP;

import scala.sys.process.ProcessBuilderImpl.Simple;

import org.apache.camel.component.http4.*;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new  RouteBuilder() {
			
			/**
			 * @author Jefferson Russo -  31/05/2021
			 * 
			 * Override configure, reponsavel por transformar um XML em um JSON
			 * obdecendo as regras no meio do trajeto entre o FROM e o TO.
			 */
			
			@Override
			public void configure() throws Exception {
				
				from("file:pedidos?delay=5s&noop=true").
				setProperty("pedidoId", xpath("/pedido/id/text()")).
				setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()")).
						split().
							xpath("/pedido/itens/item").
						filter().
							xpath("/item/formato[text()='EBOOK']").
							setProperty("ebookId",  xpath("/item/livro/codigo/text()")).
						marshal().
							xmljson().
							log("${id} - ${body}").
						setHeader(Exchange.FILE_NAME , HttpMethods.GET  ).
						setHeader(Exchange.HTTP_QUERY,  simple ("ebookId=${property.ebookId}&{property.pedidoId}&clienteId=${.property.clienteId}")).
				to("http4://localhost:8080/webservices/ebook");
					
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
		
	}	
}
