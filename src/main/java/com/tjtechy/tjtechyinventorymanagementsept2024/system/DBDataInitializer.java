package com.tjtechy.tjtechyinventorymanagementsept2024.system;

import com.tjtechy.tjtechyinventorymanagementsept2024.author.model.Author;
import com.tjtechy.tjtechyinventorymanagementsept2024.author.repository.AuthorRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.model.Book;
import com.tjtechy.tjtechyinventorymanagementsept2024.book.repository.BookRepository;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.model.LibraryUser;
import com.tjtechy.tjtechyinventorymanagementsept2024.user.service.LibraryUserService;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.hibernate.id.factory.internal.UUIDGenerationTypeStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

//I COMMENT THIS BECAUSE IT KEEPS GENERATING RECORD IN THE DB EACH TIME I START THE APPLICATION

@Component
@Profile("h2-database") //will only be loaded if active profile is h2 in memory database
//@Profile("mysql-database") //will only be loaded if active profile is mysql database. I just used this to initialize the DB in mysql db
//@Profile("postgres-database")
public class DBDataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final LibraryUserService libraryUserService;

    public DBDataInitializer(BookRepository bookRepository, AuthorRepository authorRepository, LibraryUserService libraryUserService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
      this.libraryUserService = libraryUserService;
    }



    @Override
    public void run(String... args) throws Exception {
        //create some books and authors
        var book1 = new Book();
        UUID bookIsbn1 = UUID.fromString("31a171c8-9b73-49c1-b09c-fc2f08da3b35");


        book1.setISBN(bookIsbn1);
        book1.setTitle("Book 1");
        book1.setPublisher("john publishing Oy");
        book1.setPublicationDate(new Date(1726249781000L));
        book1.setGenre("some book1 genre");
        book1.setEdition("some book1 edition");
        book1.setLanguage("some book1 language");
        book1.setPages(50);
        book1.setDescription("some book1 description");
        book1.setPrice(100.00);
        book1.setQuantity("6");

        var book2 = new Book();
        UUID bookIsbn2 = UUID.fromString("892cc179-d593-4667-8792-9069f0b078cf");
        book2.setISBN(bookIsbn2);
        book2.setTitle("Book 2");
        book2.setPublisher("jones publishing company");
        book2.setPublicationDate(new Date(1726076981000L));
        book2.setGenre("some book2 genre");
        book2.setEdition("some book2 edition");
        book2.setLanguage("some book2 language");
        book2.setPages(70);
        book2.setDescription("some book2 description");
        book2.setPrice(98.00);
        book2.setQuantity("3");

        var book3 = new Book();
        UUID bookIsbn3 = UUID.fromString("59dd83f1-9174-4ea3-938c-9637752249f1");
        book3.setISBN(bookIsbn3);
        book3.setTitle("Book 3");
        book3.setPublisher("greg publishing company");
        book3.setPublicationDate(new Date(1694368181000L));
        book3.setGenre("some book3 genre");
        book3.setEdition("some book3 edition");
        book3.setLanguage("some book3 language");
        book3.setPages(70);
        book3.setDescription("some book3 description");

        book3.setPrice(98.00);
        book3.setQuantity("7");

        var book4 = new Book();
        UUID bookIsbn4 = UUID.fromString("c38aafd0-cf59-4168-b58c-da8eab69cc86");
        book4.setISBN(bookIsbn4);
        book4.setTitle("Book 4");
        book4.setPublisher("greg publishing company");
        book4.setPublicationDate(new Date(1662832181000L));
        book4.setGenre("some book4 genre");
        book4.setEdition("some book4 edition");
        book4.setLanguage("some book4 language");
        book4.setPages(40);
        book4.setDescription("some book4 description");

        book4.setPrice(120.00);
        book4.setQuantity("10");

        var author1 = new Author();
        author1.setAuthorId(1000L);
        author1.setFirstName("Taju");
        author1.setLastName("Gani");
        author1.setEmail("taju@gmail.com");
        author1.setBiography("author 1 biography");

        author1.addBook(book1);
        author1.addBook(book4);

        var author2 = new Author();
        author2.setAuthorId(2000L);
        author2.setFirstName("lukas");
        author2.setLastName("tom");
        author2.setEmail("lukas@gmail.com");
        author2.setBiography("author 2 biography");

        author2.addBook(book2);


        var author3 = new Author();
        author3.setAuthorId(3000L);
        author3.setFirstName("James");
        author3.setLastName("Jones");
        author3.setEmail("jones@gmail.com");
        author3.setBiography("author 3 biography");

        author3.addBook(book3);

        /*this automatically saves all authors as well as books associated with them
        //because in the author class we have used @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "owner" )
        //if there is one book that is not assigned to any author, it has be saved separately by calling
        //bookRepository.save(book<n>)
        //Command to delete data from myWork bench--> DELETE FROM author WHERE author_id = ?;
        //THIS CAN BE USED AS WELL---> TRUNCATE TABLE books;
        //
        */

        authorRepository.save(author1);
        authorRepository.save(author2);
        authorRepository.save(author3);

        //create some library users
      LibraryUser user1 = new LibraryUser();
      user1.setUserId(1);
      user1.setUserName("ben");
      user1.setPassword("123456");
      user1.setEnabled(true);
      user1.setRoles("Admin user");

      LibraryUser user2 = new LibraryUser();
      user2.setUserId(2);
      user2.setUserName("kim");
      user2.setPassword("654321");
      user2.setEnabled(true);
      user2.setRoles("user");

      LibraryUser user3 = new LibraryUser();
      user3.setUserId(3);
      user3.setUserName("john");
      user3.setPassword("qwerty");
      user3.setEnabled(false);
      user3.setRoles("user");

      this.libraryUserService.save(user1);
      this.libraryUserService.save(user2);
      this.libraryUserService.save(user3);
    }
}
