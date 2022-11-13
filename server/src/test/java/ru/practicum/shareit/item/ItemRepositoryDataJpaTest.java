package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRepositoryDataJpaTest {

    private final TestEntityManager testEntityManager;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();

    @Test
    @DirtiesContext
    void searchByNameTest() {
        userRepository.save(owner);
        itemRepository.save(item);

        TypedQuery<Item> query = testEntityManager.getEntityManager()
                .createQuery(" select i from Item i " +
                        "where lower(i.name) like lower(concat('%', ?1, '%')) " +
                        "or lower(i.description) like lower(concat('%', ?1, '%')) " +
                        "and i.available = true", Item.class);

        Item res = query.setParameter(1, item.getName()).getSingleResult();

        assertEquals(res.getId(), item.getId());
        assertEquals(res.getName(), item.getName());

    }

    @Test
    @DirtiesContext
    void searchByDescriptionTest() {
        userRepository.save(owner);
        itemRepository.save(item);

        TypedQuery<Item> query = testEntityManager.getEntityManager()
                .createQuery(" select i from Item i " +
                        "where lower(i.name) like lower(concat('%', ?1, '%')) " +
                        "or lower(i.description) like lower(concat('%', ?1, '%')) " +
                        "and i.available = true", Item.class);

        Item res = query.setParameter(1, item.getDescription()).getSingleResult();

        assertEquals(res.getId(), item.getId());
        assertEquals(res.getDescription(), item.getDescription());

    }

    @Test
    @DirtiesContext
    void searchByNameTestFailsWhenAvailableIsFalse() {
        item.setAvailable(false);
        userRepository.save(owner);
        itemRepository.save(item);

        TypedQuery<Item> query = testEntityManager.getEntityManager()
                .createQuery(" select i from Item i " +
                        "where lower(i.name) like lower(concat('%', ?1, '%')) " +
                        "or lower(i.description) like lower(concat('%', ?1, '%')) " +
                        "and i.available = true", Item.class);

        NoResultException exception = assertThrows(NoResultException.class,
                () -> query.setParameter(1, item.getDescription()).getSingleResult());

        assertEquals("No entity found for query", exception.getMessage());

    }
}
