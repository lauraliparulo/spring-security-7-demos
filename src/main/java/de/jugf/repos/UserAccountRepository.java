package de.jugf.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import de.jugf.entities.users.UserAccount;


public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
	
	UserAccount findByUsername(String username);
	
}