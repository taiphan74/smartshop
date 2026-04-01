package com.ptithcm.smartshop.seed;

import com.ptithcm.smartshop.entity.Category;
import com.ptithcm.smartshop.repository.CategoryRepository;
import com.ptithcm.smartshop.util.SlugUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CategorySeeder implements ApplicationRunner {

    private static final List<CategoryNode> CATEGORY_TREE = List.of(
            node("Thời trang",
                    node("Nam",
                            node("Áo",
                                    node("Áo thun"),
                                    node("Áo sơ mi"),
                                    node("Áo hoodie"),
                                    node("Áo khoác")
                            ),
                            node("Quần",
                                    node("Quần jeans"),
                                    node("Quần short"),
                                    node("Quần tây")
                            ),
                            node("Giày dép",
                                    node("Sneaker"),
                                    node("Sandal"),
                                    node("Dép")
                            ),
                            node("Phụ kiện",
                                    node("Nón"),
                                    node("Thắt lưng"),
                                    node("Đồng hồ")
                            )
                    ),
                    node("Nữ",
                            node("Áo"),
                            node("Váy"),
                            node("Quần"),
                            node("Giày dép"),
                            node("Túi xách")
                    ),
                    node("Trẻ em")
            ),
            node("Điện tử",
                    node("Điện thoại",
                            node("Smartphone"),
                            node("Feature phone"),
                            node("Phụ kiện",
                                    node("Ốp lưng"),
                                    node("Sạc"),
                                    node("Tai nghe")
                            )
                    ),
                    node("Laptop",
                            node("Laptop gaming"),
                            node("Laptop văn phòng"),
                            node("Phụ kiện")
                    ),
                    node("Máy tính bảng"),
                    node("Thiết bị âm thanh",
                            node("Loa"),
                            node("Tai nghe"),
                            node("Micro")
                    ),
                    node("Smart home")
            ),
            node("Nhà cửa",
                    node("Nội thất",
                            node("Bàn"),
                            node("Ghế"),
                            node("Tủ")
                    ),
                    node("Nhà bếp",
                            node("Nồi"),
                            node("Chảo"),
                            node("Dụng cụ nấu ăn")
                    ),
                    node("Gia dụng",
                            node("Máy giặt"),
                            node("Máy hút bụi"),
                            node("Quạt")
                    ),
                    node("Trang trí",
                            node("Tranh"),
                            node("Đèn"),
                            node("Cây cảnh")
                    ),
                    node("Dụng cụ sửa chữa")
            ),
            node("Làm đẹp",
                    node("Skincare",
                            node("Sữa rửa mặt"),
                            node("Toner"),
                            node("Serum"),
                            node("Kem dưỡng")
                    ),
                    node("Makeup",
                            node("Son"),
                            node("Kem nền"),
                            node("Phấn")
                    ),
                    node("Chăm sóc tóc",
                            node("Dầu gội"),
                            node("Dầu xả"),
                            node("Serum tóc")
                    ),
                    node("Nước hoa"),
                    node("Dụng cụ làm đẹp")
            ),
            node("Mẹ & Bé",
                    node("Sữa"),
                    node("Tã bỉm"),
                    node("Đồ chơi"),
                    node("Quần áo trẻ em"),
                    node("Đồ dùng cho bé",
                            node("Bình sữa"),
                            node("Xe đẩy"),
                            node("Ghế ăn")
                    )
            ),
            node("Thực phẩm",
                    node("Đồ ăn nhanh"),
                    node("Đồ khô",
                            node("Mì"),
                            node("Gạo"),
                            node("Gia vị")
                    ),
                    node("Đồ uống",
                            node("Nước ngọt"),
                            node("Cà phê"),
                            node("Trà")
                    ),
                    node("Thực phẩm tươi",
                            node("Rau"),
                            node("Thịt"),
                            node("Hải sản")
                    ),
                    node("Đặc sản vùng miền")
            ),
            node("Thể thao",
                    node("Gym & Fitness",
                            node("Tạ"),
                            node("Máy tập"),
                            node("Dây kháng lực")
                    ),
                    node("Thể thao ngoài trời"),
                    node("Du lịch",
                            node("Balo"),
                            node("Vali"),
                            node("Phụ kiện du lịch")
                    )
            ),
            node("Sách",
                    node("Sách giáo khoa"),
                    node("Sách kỹ năng"),
                    node("Tiểu thuyết"),
                    node("Văn phòng phẩm",
                            node("Bút"),
                            node("Sổ"),
                            node("Giấy")
                    ),
                    node("Dụng cụ học tập")
            ),
            node("Ô tô - Xe máy",
                    node("Phụ kiện ô tô"),
                    node("Phụ kiện xe máy"),
                    node("Đồ bảo hộ",
                            node("Mũ bảo hiểm"),
                            node("Áo giáp")
                    ),
                    node("Dầu nhớt")
            ),
            node("Tiêu dùng",
                    node("Đồ vệ sinh"),
                    node("Giặt giũ"),
                    node("Chăm sóc cá nhân",
                            node("Kem đánh răng"),
                            node("Sữa tắm"),
                            node("Dầu gội")
                    ),
                    node("Giấy & khăn")
            )
    );

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (CategoryNode root : CATEGORY_TREE) {
            upsertCategory(root, null, null, 0);
        }
    }

    private Category upsertCategory(CategoryNode node, Category parent, String parentSlug, int level) {
        String ownSlug = buildSlug(node.name(), parentSlug);
        String ownPath = buildPath(parent, ownSlug);

        Category category = categoryRepository.findBySlug(ownSlug).orElseGet(Category::new);
        category.setName(node.name());
        category.setSlug(ownSlug);
        category.setPath(ownPath);
        category.setLevel(level);
        category.setParent(parent);

        Category saved = categoryRepository.save(category);
        for (CategoryNode child : node.children()) {
            upsertCategory(child, saved, ownSlug, level + 1);
        }
        return saved;
    }

    private String buildSlug(String name, String parentSlug) {
        String currentSlug = SlugUtil.toSlug(name);
        if (parentSlug == null || parentSlug.isBlank()) {
            return currentSlug;
        }
        return parentSlug + "-" + currentSlug;
    }

    private String buildPath(Category parent, String slug) {
        if (parent == null) {
            return "/" + slug;
        }
        return parent.getPath() + "/" + slug;
    }

    private static CategoryNode node(String name, CategoryNode... children) {
        return new CategoryNode(name, List.of(children));
    }

    private record CategoryNode(String name, List<CategoryNode> children) {
    }
}
