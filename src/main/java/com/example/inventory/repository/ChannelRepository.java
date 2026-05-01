package com.example.inventory.repository;

import com.example.inventory.domain.channel.Channel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByNameIgnoreCase(String name);
}
