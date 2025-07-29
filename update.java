///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavencentral,jitpack
//DEPS io.quarkus:quarkus-bom:${quarkus.version:3.25.0}@pom
//DEPS io.quarkus:quarkus-qute
//DEPS com.apptasticsoftware:rssreader:3.9.3
//DEPS io.github.furstenheim:copy_down:1.1
//DEPS io.github.markdown-asciidoc:markdown-to-asciidoc:2.0.1


//DEPS io.quarkus:quarkus-rest-client
//DEPS io.quarkus:quarkus-rest-client-jackson

//JAVA 21+

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import io.github.furstenheim.CopyDown;
import io.quarkus.qute.Engine;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import nl.jworks.markdown_to_asciidoc.Converter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@QuarkusMain
public class update implements QuarkusApplication {

    @Inject
    Engine qute;

   // @RestClient
   // PlaylistService playlistService;

    public int run(String... args) throws Exception {

        String bio = """
                Max works as Distinguished Engineer at Red Hat, currently as part of the Quarkus team focusing on Developer joy.\s
                        
                Developer joy plays a central part of Max’s 15+ years of experience as a professional open-source contributor. Max worked on Hibernate/Hibernate Tools, WildFly, Seam, and Ceylon. Max led the team behind JBoss Tools and Developer Studio until starting work on Quarkus.
                        
                Quarkus being a Kubernetes native stack keeps Max busy ensuring developers still experience joy deploying Quarkus applications to Kubernetes platforms like OpenShift.
                        
                Max has a keen interest in moving the Java ecosystem forward and making it more accessible.
                To that end he created https://jbang.dev[JBang] a tool to bring back developer joy to Java and works closely with teams defining and exploring making native image for Java a reality using GraalVM/Mandrel, Leyden and Quarkus.
                        
                Max also co-hosts the weekly video podcast called https://quarkus.io/insights[Quarkus Insights] and he can be found on twitter as https://twitter.com/@maxandersen[@maxandersen]
                """;


        Collection<Item> sorted = new PriorityQueue<>();
        RssReader reader = new RssReader();
        Stream<Item> rssFeed = reader.read("https://xam.dk/blog/feed.atom");
        sorted.addAll(rssFeed.limit(3).collect(Collectors.toList()));

        sorted.addAll(reader.read("https://quarkus.io/feed.xml")
                        .filter(p->p.getAuthor().get().contains("/maxandersen")).limit(3)
                        .map(p->{return extracted(p);})
                        .collect(Collectors.toList()));
 
        Files.writeString(Path.of("readme.adoc"), qute.parse(Files.readString(Path.of("template.adoc.qute")))
                .data("bio", bio)
                .data("posts", sorted)
               // .data("video", video)
                .render());

        return 0;
    }
    private Item extracted(Item p) {
        p.setDescription(Converter.convertMarkdownToAsciiDoc(new CopyDown().convert(p.getDescription().get())));
        
        p.setDescription(p.getDescription().get().substring(0, p.getDescription().get().indexOf("\n")));
        
        return p;
    }

 static public class Items{

 }
     @RegisterRestClient(baseUri = "https://www.googleapis.com")
    public static interface PlaylistService {

        @GET
        @jakarta.ws.rs.Path("/youtube/v3/playlistItems")
        List<Items> playlist(@QueryParam("part") String part,
                          @QueryParam("maxResults") int maxResults,
                          @QueryParam("playlistId") String playListId,
                          @QueryParam("key") String key);
    }

    }

