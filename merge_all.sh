#!/usr/bin/env bash
set -e
BR=merge/develop-into-main-final

git checkout -- mvnw 2>/dev/null || true
git fetch --all --prune -q
git merge --abort 2>/dev/null || true
git checkout -q main
git pull -q --ff-only origin main
git branch -D "$BR" 2>/dev/null || true
git checkout -q -b "$BR"

echo ">> Merge develop..."
git merge --no-edit origin/develop >/dev/null 2>&1 || true

echo ">> Giải quyết xung đột..."
for f in $(git diff --name-only --diff-filter=U); do
  case "$f" in
    README.md) git checkout --ours "$f" ;;
    src/main/java/com/oneshop/entity/Product.java) ;;
    *) git checkout --theirs "$f" ;;
  esac
done

cat > src/main/java/com/oneshop/entity/Product.java <<'EOF'
package com.oneshop.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;
    @Column(length = 4000)
    private String description;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int discountPercent = 0;

    @Builder.Default
    private int stock = 0;
    @Builder.Default
    private int sold = 0;
    @Builder.Default
    private double avgRating = 0;
    private String brand;
    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private Boolean locked = false;

    private LocalDateTime createdAt;

    public boolean isLocked() {
        return Boolean.TRUE.equals(locked);
    }

    public BigDecimal getFinalPrice() {
        if (price == null || discountPercent <= 0)
            return price;
        BigDecimal keep = BigDecimal.valueOf(100 - Math.min(discountPercent, 100));
        return price.multiply(keep).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
EOF

cat > src/main/java/com/oneshop/repository/PromotionRepository.java <<'EOF'
package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import com.oneshop.common.PromotionScope;
import com.oneshop.entity.Promotion;
import jakarta.persistence.LockModeType;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByIdAndScope(Long id, PromotionScope scope);
    Page<Promotion> findByShopId(Long shopId, Pageable pageable);
    Optional<Promotion> findByIdAndShopId(Long id, Long shopId);
    boolean existsByCodeIgnoreCase(String code);
    Optional<Promotion> findByCodeIgnoreCase(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Promotion> findWithLockByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = "shop")
    Page<Promotion> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "shop")
    Page<Promotion> findByScope(PromotionScope scope, Pageable pageable);
}
EOF

git rm -qf src/main/java/com/oneshop/admin/AdminPlatformPromotionController.java \
           src/main/java/com/oneshop/admin/AdminPlatformPromotionService.java 2>/dev/null || true
git checkout -q origin/develop -- src/test/java/com/oneshop/VendorFlowTests.java 2>/dev/null || true

python3 - <<'PY'
import subprocess
p='src/main/webapp/WEB-INF/decorators/web.jsp'
main=subprocess.run(['git','show','origin/main:'+p],capture_output=True,text=True,encoding='utf-8').stdout
cur=open(p,encoding='utf-8').read()
BS='<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js">'
if 'beauty-assistant' not in cur and 'data-beauty-assistant' in main:
    a=main.index('<div class="beauty-assistant" data-beauty-assistant>')
    b=main.index(BS)
    widget=main[a:b]
    i=cur.index('<link href="/css/oneshop.css" rel="stylesheet">')
    j=cur.index('\n',i)+1
    cur=cur[:j]+'    <link href="/css/beauty-assistant.css" rel="stylesheet">\n'+cur[j:]
    k=cur.index(BS)
    ls=cur.rfind('\n',0,k)+1
    cur=cur[:ls]+widget+cur[ls:]
    k=cur.index(BS)
    e=cur.index('\n',k)+1
    cur=cur[:e]+'<script src="/js/beauty-assistant.js" defer></script>\n'+cur[e:]
    open(p,'w',encoding='utf-8').write(cur)
    print(">> Đã giữ trợ lý Gemini trong web.jsp")
PY

if grep -rl '^<<<<<<<' src >/dev/null 2>&1; then
  echo "!! Còn file xung đột:"; grep -rl '^<<<<<<<' src; exit 1
fi
git add -A
git -c user.name="$(git config user.name || echo dev)" -c user.email="$(git config user.email || echo dev@local)" commit -q -m "merge: gộp develop vào main"
echo ">> Merge xong trên nhánh $BR. Chạy app..."
sh ./mvnw -Dmaven.test.skip=true -Dspring-boot.run.profiles=demo spring-boot:run
